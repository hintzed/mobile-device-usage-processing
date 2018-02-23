package de.fhdw.deviceanalyzer.parser.core;
public enum DeviceState {

		LOCKED_DISPLAY_OFF {
			@Override
			public DeviceState nextState(Event event) {
				if ("screen|power".equals(event.key) && event.value.startsWith("on")) return LOCKED_DISPLAY_ON;
				if ("phone|ringing".equals(event.key)) return LOCKED_RINGING;
				else return this;
			}
		},
		LOCKED_DISPLAY_ON {
			@Override
			public DeviceState nextState(Event event) {
				if ("screen|power".equals(event.key) && event.value.startsWith("off")) return LOCKED_DISPLAY_OFF;
				if ("shutdown".equals(event.key)) return LOCKED_DISPLAY_OFF;
				if ("phone|keyguardremoved".equals(event.key)) return UNLOCKED;
				// if ("hf|locked".equals(event.key) && "false".equals(value)) return UNLOCKED; // Results in unstable results sometimes
				else return this;
			}
		},
		UNLOCKED {
			@Override
			public DeviceState nextState(Event event) {
				if ("screen|power".equals(event.key) && event.value.startsWith("off")) return LOCKED_DISPLAY_OFF;
				if ("shutdown".equals(event.key)) return LOCKED_DISPLAY_OFF;
				//if ("hf|locked".equals(event.key) && "true".equals(event.value)) return LOCKED_DISPLAY_ON;
				if ("phone|calling".equals(event.key)) return UNLOCKED_CALL;
				if ("phone|ringing".equals(event.key)) return UNLOCKED_CALL;
				else return this;
			}
		},
		
		UNLOCKED_CALL {
			@Override
			public DeviceState nextState(Event event) {
				// The next event should be "offhook", otherwise defensively assume no call
				if ("phone|offhook".equals(event.key)) return UNLOCKED_ACTIVE_CALL;
				else return UNLOCKED;
			}
			
		},
		
		UNLOCKED_ACTIVE_CALL {
			@Override
			public DeviceState nextState(Event event) {
				if ("phone|idle".equals(event.key)) return UNLOCKED;
				else return this;
			}
		},
		LOCKED_RINGING {

			@Override
			public DeviceState nextState(Event event) {
				if ("phone|idle".equals(event.key)) return LOCKED_DISPLAY_OFF;
			if ("phone|offhook".equals(event.key)) return LOCKED_ACTIVE_CALL;
			else return this;
			}

		},
		LOCKED_ACTIVE_CALL {

			@Override
			public DeviceState nextState(Event event) {
				if ("phone|idle".equals(event.key)) return LOCKED_DISPLAY_ON;
				return this;
			}

		};

		public abstract DeviceState nextState(Event event);
	}