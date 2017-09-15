import {diInject} from '../app/DIContext';
import {observable} from 'mobx';
import {Rule} from './model';
import {ScriptService} from './ScriptService';
import {SelectCallback} from 'react-bootstrap';

export class ScriptStore {
  createScriptViewStore() {
    return new ScriptViewStore();
  }
}

export class ScriptViewStore {
  @diInject() scriptService: ScriptService;

  @observable rules: Rule[] = [];
  @observable selectedRuleId: string;
  @observable selectedRuleScript: string;
  @observable runStatus: string;

  constructor() {
    this.scriptService.getRules().subscribe(
      rules => this.rules = rules
    )
  }

  selectRule: SelectCallback = (rule) => {
    this.selectedRuleId = (rule as Rule).id;
    this.selectedRuleScript = (rule as Rule).script;
  };

  runScript = () =>
    this.scriptService
      .runScript(this.selectedRuleScript)
      .subscribe(v => this.runStatus = v.log);

  updateScript = (script) => this.selectedRuleScript = script;
}
