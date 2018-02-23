package de.fhdw.deviceanalyzer.parser.cell;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import de.fhdw.deviceanalyzer.parser.core.ContextUtil;

public class CellContextLocationGenerator {

	public static final double WEEKEND_THRESHOLD = 0.2;
	public static final double WORKTIME_THRESHOLD = 0.5;
	public static final double NIGHTTIME_THRESHOLD = 0.25;
	public static final double FREETIME_THERSHOLD = 0.7;
	public static final Integer CARDINALITY_THRESHOLD = 2;
	public static final int QUALIFIED_CELL_THRESHOLD = 10;

	public static final int TIMEOUT_MIN = 5;

	public static final Cell MISSING_CELL = new Cell( null, null);

	private List<CellEvent> cellPath = new ArrayList<>();

	public void addCell(Cell cell, Date timestamp) {
		CellEvent event = new CellEvent(timestamp, cell);
		CellEvent previousEvent = Iterables.getFirst(cellPath, null);

		// Handle missing events
		if (previousEvent != null
				&& previousEvent.cell != MISSING_CELL
				&& event.cell != MISSING_CELL
				&& Duration.between(Instant.ofEpochMilli(previousEvent.ts.getTime()), Instant.ofEpochMilli(event.ts.getTime())).toMinutes() > TIMEOUT_MIN) {
			cellPath.add(new CellEvent(null, MISSING_CELL));
		}

		cellPath.add(event);
	}

	public List<CellContextLocation> getLocations() {
		Set<Cell> cells = cellPath.stream().map(c -> c.cell).filter(c -> c != null && c != MISSING_CELL).collect(Collectors.toSet());
		Set<Set<Cell>> minimalCircularSubsequences = findMinimalCircularSubsequences(cellPath);
		List<Cell> qualifiedCells = findQualifiedCells(cellPath);

		Set<Set<Cell>> clusters = findClusters(cells, minimalCircularSubsequences, qualifiedCells);

		List<CellContextLocation> locations = findLocations(cellPath, clusters);

		ContextUtil.tagMeaningfullLocations(locations);
		
		return locations;
	}


	private List<CellContextLocation> findLocations(List<CellEvent> cellPath, Set<Set<Cell>> clusters) {
		List<CellContextLocation> locations = new ArrayList<>();
		int i = 0;

		for (Set<Cell> cluster : clusters) {
			CellContextLocation cellContextLocation = new CellContextLocation();
			cellContextLocation.id = String.format("cluster_%d", ++i);
			cellContextLocation.cells = cluster;

			for (CellEvent event : cellPath) {
				if (cluster.contains(event.cell)) {
					cellContextLocation.count(event.ts);
				}
			}

			locations.add(cellContextLocation);
		}

		Collections.sort(locations);

		return locations;
	}

	private Set<Set<Cell>> findClusters(Set<Cell> cells, Set<Set<Cell>> minimalCircularSubsequences, List<Cell> clusterRoots) {
		Set<Set<Cell>> clusters = new HashSet<>();

		Set<Cell> alreadyClustered = new HashSet<>();
		
		while (!clusterRoots.isEmpty()) {
			Cell clusterRoot = clusterRoots.remove(0);

			Set<Cell> currentCluster = new HashSet<>();
			currentCluster.add(clusterRoot);

			List<Set<Cell>> toRemove = new ArrayList<>();
			for (Set<Cell> circularSubsequence : minimalCircularSubsequences) {
				if (circularSubsequence.contains(clusterRoot)) {
					currentCluster.addAll(circularSubsequence);
					toRemove.add(circularSubsequence);
				}
			}

			minimalCircularSubsequences.removeAll(toRemove);
			clusterRoots.removeAll(currentCluster);

			currentCluster.removeAll(alreadyClustered);
			clusters.add(currentCluster);
			alreadyClustered.addAll(currentCluster);
		}

		cells.removeAll(alreadyClustered);

		// Add remaining cells not yet clustered
		for (Cell singleCell : cells) {
			clusters.add(Sets.newHashSet(singleCell));
		}

		return clusters;
	}

	private Set<Set<Cell>> findMinimalCircularSubsequences(List<CellEvent> cellPath) {
		Set<Set<Cell>> minimalCircularSubsequences = new HashSet<>();
		LinkedList<Cell> window = new LinkedList<>();

		Cell previousCell = null;
		for (CellEvent cellEvent : cellPath) {
			Cell cell = cellEvent.cell;

			// Avoid having same cell multiple times in a row
			if (Objects.equals(cell, previousCell)) continue;

			// Reset Window on missing Data
			if (cell == null || cell.cellid == null || cell == MISSING_CELL) {
				window.clear();
				continue;
			}

			if (window.contains(cell)) {
				HashSet<Cell> circularSubsequences = new HashSet<>(window.subList(window.indexOf(cell), window.size()));

				minimalCircularSubsequences.add(circularSubsequences);

				window.clear();
			}

			window.add(cell);
			if (window.size() > CARDINALITY_THRESHOLD) window.removeFirst();

			previousCell = cell;
		}

		return minimalCircularSubsequences;
	}

	private List<Cell> findQualifiedCells(List<CellEvent> cellPath) {
		Map<Cell, List<CellEvent>> groupByCells = cellPath
				.stream()
				.filter(c -> c != null && c.cell != MISSING_CELL)
				.collect(Collectors.groupingBy(CellEvent::getCell));

		return groupByCells
				.keySet()
				.stream()
				.sorted((a, b) -> Integer.compare(groupByCells.get(a).size(), groupByCells.get(b).size()))
				.filter(k -> groupByCells.get(k)
						.stream()
						.filter(dc -> dc.ts != null)
						.collect(Collectors.groupingBy(CellEvent::getLocalDate, Collectors.counting()))
						.values()
						.stream()
						.max(Long::compare)
						.orElse(0L) >= QUALIFIED_CELL_THRESHOLD)
				.collect(Collectors.toList());
	}


}
