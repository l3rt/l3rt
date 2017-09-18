import {Observable} from 'rx';
import {ExecutionResult, RunRequest} from './model';
import {Rest} from '../app/Rest';
import {Config} from '../app/Config';

export class ScriptService {
    getRules(): Observable<RunRequest[]> {
        return Rest.doGet<RunRequest[]>(`${Config.BASE_URL}/rules`);
    }

    runScript(script: string, mockTargets: boolean): Observable<ExecutionResult> {
        return Rest.doPost<any, ExecutionResult>(`${Config.BASE_URL}/runScript`, {script, mockTargets});
    }
}
