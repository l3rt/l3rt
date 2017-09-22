import {diInject} from '../app/DIContext';
import {observable} from 'mobx';
import {LogEvent, Rule} from './model';
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
    @observable runStatus: Array<LogEvent> = [];
    @observable mockTargets: boolean = true;
    @observable showSaveDialog = false;
    @observable ruleName = "";
    @observable saveError;

    constructor() {
        this.reloadRules();
    }

    private reloadRules = () => {
        this.scriptService.getRules().subscribe(
            rules => this.rules = rules
        )
    };

    selectRule: SelectCallback = (rule) => {
        this.selectedRuleId = (rule as Rule).id;
        this.selectedRuleScript = (rule as Rule).script;
        this.ruleName = this.selectedRuleId;
    };

    runScript = () =>
        this.scriptService
            .runScript(this.selectedRuleScript, this.mockTargets)
            .subscribe(v => this.runStatus = v.log);

    saveDialog = () => this.showSaveDialog = true;
    closeSaveDialog = () => this.showSaveDialog = false;

    updateScript = (script) => this.selectedRuleScript = script;

    setMockTargets = (e) => this.mockTargets = e.target.checked;

    changeRuleName = (e) => this.ruleName = e.target.value;

    saveRule = () => {
        console.log("save");
        this.scriptService
            .saveRule(this.ruleName, this.selectedRuleScript)
            .subscribe(
                () => {
                    this.reloadRules();
                    this.closeSaveDialog();
                },
                e => this.saveError = e.message
            );
    }

}
