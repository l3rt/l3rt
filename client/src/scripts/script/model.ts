export interface Rule {
  id: string;
  script: string;
}

export interface ExecutionResult {
    log: Array<LogEvent>;
}

export interface LogEvent {
    level: string;
    time: number;
    message: string;
}
