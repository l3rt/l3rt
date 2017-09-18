export interface RunRequest {
  id: string;
  script: string;
  mockTargets: boolean;
}

export interface ExecutionResult {
    log: Array<LogEvent>;
}

export interface LogEvent {
    level: string;
    time: number;
    message: string;
}
