package de.fhdw.deviceanalyzer.parser.core;

import java.util.Date;

import com.google.common.collect.ComparisonChain;

import de.fhdw.deviceanalyzer.parser.cell.CellContextLocationGenerator;

public abstract class AbstractContextLocation implements Comparable<AbstractContextLocation> {

	private long weekdays;
	private long weekend;
	private long total;
	private long workingHourOnWeekday;
	private long nightHourOnWeekday;
	private long otherThanWorkingHourOnWeekday;
	private boolean isMeaningful;

	public enum Context {
		OFFICE, HOME, OTHER_MEANINGFUL, ELSEWHERE, NO_DATA
	}

	public long getTotal() {
		return total;
	}

	public boolean isMeaningful() {
		return isMeaningful;
	}

	public void setMeaningful() {
		this.isMeaningful = true;
	}

	public void count(Date ts) {
		total++;

		boolean nightHour = ContextUtil.isNightHour(ts);
		boolean weekday = ContextUtil.isWeekday(ts);
		boolean workingHour = ContextUtil.isWorkingHour(ts);

		if (weekday) weekdays++;
		if (!weekday) weekend++;

		if (workingHour && weekday) workingHourOnWeekday++;
		if (nightHour && weekday) nightHourOnWeekday++;
		if (!workingHour && weekday) otherThanWorkingHourOnWeekday++;
	}

	public Context getContext() {
		if (isMeaningful && isHome()) return Context.HOME;
		if (isMeaningful && isOffice()) return Context.OFFICE;
		if (isMeaningful) return Context.OTHER_MEANINGFUL;
		return Context.ELSEWHERE;
	}

	private boolean isOffice() {
		return weekdays > 0
				&& 1f * weekend / total < CellContextLocationGenerator.WEEKEND_THRESHOLD
				&& 1f * workingHourOnWeekday / weekdays > CellContextLocationGenerator.WORKTIME_THRESHOLD;
	}

	private boolean isHome() {
		return weekdays > 0
				&& 1f * nightHourOnWeekday / weekdays > CellContextLocationGenerator.NIGHTTIME_THRESHOLD
				&& 1f * otherThanWorkingHourOnWeekday / weekdays > CellContextLocationGenerator.FREETIME_THERSHOLD
				&& !isOffice();
	}

	public abstract String getId();

	@Override
	public String toString() {
		return "AbstractContextLocation [isHome()=" + isHome() + ", isOffice()=" + isOffice() + ", id=" + getId() + ", weekdays=" + weekdays
				+ ", weekend="
				+ weekend
				+ ", total=" + total + ", workingHourOnWeekday=" + workingHourOnWeekday + ", nightHourOnWeekday=" + nightHourOnWeekday
				+ ", otherThanWorkingHourOnWeekday=" + otherThanWorkingHourOnWeekday + "]";
	}

	@Override
	public int compareTo(AbstractContextLocation other) {
		return ComparisonChain
				.start()
				.compareTrueFirst(isHome(), other.isHome())
				.compareTrueFirst(isOffice(), other.isOffice())
				.compare(total, other.total)
				.result();
	}

}
