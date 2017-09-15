import * as React from 'react';
import {SettingsView} from '../settigs/SettingsView';
import {ScriptView} from '../script/ScriptView';

export const SCRIPTS_MAPPING = '/scripts';

export class RouteStore {
  routes: Array<Route> = [
    {
      title: 'Scripts',
      icon: 'tasks',
      mapping: SCRIPTS_MAPPING,
      component: ScriptView
    },
    {
      title: 'Settings',
      icon: 'cogs',
      mapping: '/settings',
      component: SettingsView
    }
  ];
}

interface Route {
  title?: string;
  icon?: string;
  mapping: string;
  component: React.ReactType;
}
