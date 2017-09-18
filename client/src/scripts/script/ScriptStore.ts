import {diInject} from '../app/DIContext';
import {observable} from 'mobx';
import {LogEvent, RunRequest} from './model';
import {ScriptService} from './ScriptService';
import {SelectCallback} from 'react-bootstrap';

export class ScriptStore {
    createScriptViewStore() {
        return new ScriptViewStore();
    }
}

export class ScriptViewStore {
    @diInject() scriptService: ScriptService;

    @observable rules: RunRequest[] = [];
    @observable selectedRuleId: string;
    @observable selectedRuleScript: string;
    @observable runStatus: Array<LogEvent> = [];
    @observable mockTargets: boolean = true;

    constructor() {
        this.scriptService.getRules().subscribe(
            rules => this.rules = rules
        )
    }

    selectRule: SelectCallback = (rule) => {
        this.selectedRuleId = (rule as RunRequest).id;
        this.selectedRuleScript = (rule as RunRequest).script;
    };

    runScript = () =>
        this.scriptService
            .runScript(this.selectedRuleScript, this.mockTargets)
            .subscribe(v => this.runStatus = v.log);

    updateScript = (script) => this.selectedRuleScript = script;

    setMockTargets = (e) => this.mockTargets = e.target.checked;
}
