import * as React from 'react';
import {BrowserRouter as Router, Redirect, Route, Switch} from 'react-router-dom';
import {Provider} from 'mobx-react';
import {Menu} from './route/Menu';
import {RouteStore, SCRIPTS_MAPPING} from './route/RouteStore';
import '../styles/light-bootstrap-dashboard.css';
import '../styles/styles.scss';
import {diContext} from './app/DIContext';
import {ScriptStore} from './script/ScriptStore';
import {ScriptService} from './script/ScriptService';

export class App extends React.Component<any, any> {

  constructor(props: any, context: any) {
    super(props, context);
    diContext.routeStore = new RouteStore();
    diContext.scriptStore = new ScriptStore();
    diContext.scriptService = new ScriptService();
  }

  render() {
    return (
      <Provider {...diContext}>
        <Router>
          <Container>
            <Switch>
              {diContext.routeStore.routes
                .map(r => <Route key={r.mapping} exact path={r.mapping} component={r.component}/>)}
              <Redirect from="/" to={SCRIPTS_MAPPING}/>
              <Route component={NotFound}/>
            </Switch>
          </Container>
        </Router>
      </Provider>
    )
  }
}

export const Container = (props: { children?: any }) =>
  <div className="wrapper">
    <div className="sidebar">
      <div className="sidebar-wrapper">
        <Menu/>
      </div>
    </div>
    {props.children}
  </div>;

export const NotFound = () => <div>Page not found</div>;
