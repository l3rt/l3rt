import * as React from 'react';
import {observer} from 'mobx-react';
import {diInject} from '../app/DIContext';
import {ScriptStore, ScriptViewStore} from './ScriptStore';
import AceEditor from 'react-ace';
import {DropdownButton, MenuItem, Button, Checkbox, Panel, Modal, Alert} from 'react-bootstrap';
import 'brace/mode/groovy';
import 'brace/theme/tomorrow';
import FontAwesome = require('react-fontawesome');

interface Props {
}

@observer
export class ScriptView extends React.Component<Props, any> {

    @diInject() scriptStore: ScriptStore;
    viewStore: ScriptViewStore;

    constructor(props: Props, context: any) {
        super(props, context);
        this.viewStore = this.scriptStore.createScriptViewStore();
    }

    render() {
        return (
            <div className="main-panel">
                <nav className="navbar navbar-default navbar-fixed">
                    <div className="container-fluid">
                        <DropdownButton title={this.viewStore.selectedRuleId || 'Select a Rule'} id="scriptDropdown"
                                        onSelect={this.viewStore.selectRule}>
                            {this.viewStore.rules.map(r => <MenuItem eventKey={r} key={r.id}>{r.id}</MenuItem>)}
                        </DropdownButton>&nbsp;

                        <Button onClick={this.viewStore.runScript}><FontAwesome name="play"/></Button>&nbsp;
                        <Button onClick={this.viewStore.saveDialog}><FontAwesome name="save"/></Button>
                    </div>
                </nav>

                <div className="content">
                    <div className="row">
                        <div className="col-md-6">
                            <Panel>
                                <AceEditor mode="groovy"
                                           theme="tomorrow"
                                           name="codeEditor"
                                           value={this.viewStore.selectedRuleScript}
                                           onChange={this.viewStore.updateScript}/>
                            </Panel>
                            <Checkbox checked={this.viewStore.mockTargets} onChange={this.viewStore.setMockTargets}>
                                Use sandbox (real messages won't be sent)
                            </Checkbox>
                        </div>

                        <div className="col-md-6">
                            <Panel>
                                <div>
                                    <h2>Execution Log</h2>
                                </div>
                                {this.viewStore.runStatus.map((l, i) =>
                                    <div key={i} className="row">
                                        <div
                                            className="col-md-4 log-message-date">{new Date(l.time).toISOString()}</div>
                                        <div className={'col-md-8 log-message-' + l.level}>
                                            [{l.level}] {l.message}</div>
                                    </div>
                                )}
                            </Panel>
                        </div>
                    </div>
                </div>

                <Modal
                    show={this.viewStore.showSaveDialog} onHide={this.viewStore.closeSaveDialog}>
                    <Modal.Header closeButton>
                        <Modal.Title>Save Rule</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        {(this.viewStore.saveError) ? <Alert bsStyle="danger">{this.viewStore.saveError}</Alert> : null}

                        Rule name&nbsp; <input value={this.viewStore.ruleName}
                                               onChange={this.viewStore.changeRuleName}/>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button onClick={this.viewStore.saveRule}>Save</Button>
                        <Button onClick={this.viewStore.closeSaveDialog}>Cancel</Button>
                    </Modal.Footer>
                </Modal>
            </div>);
    }
}