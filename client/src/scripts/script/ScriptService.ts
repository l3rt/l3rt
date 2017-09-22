import {Observable} from 'rx';
import {ExecutionResult, Rule} from './model';
import {Rest} from '../app/Rest';
import {Config} from '../app/Config';

export class ScriptService {
    getRules(): Observable<Rule[]> {
        return Rest.doGet<Rule[]>(`${Config.BASE_URL}/rules`);
    }

    runScript(script: string, mockTargets: boolean): Observable<ExecutionResult> {
        return Rest.doPost<any, ExecutionResult>(`${Config.BASE_URL}/runScript`, {script, mockTargets});
    }

    saveRule(id: string, script: string) {
        return Rest.doPost<Rule, void>(`${Config.BASE_URL}/rules`, {id, script});
    }
}
