import {Observable} from 'rx';
import {Rule} from './model';
import {Rest} from '../app/Rest';
import {Config} from '../app/Config';

export class ScriptService {
  getRules(): Observable<Rule[]> {
    return Rest.doGet<Rule[]>(`${Config.BASE_URL}/rules`);
  }

  runScript(script: string): Observable<any> {
    return Rest.doPost<any, any>(`${Config.BASE_URL}/runScript`, {script});
  }
}
