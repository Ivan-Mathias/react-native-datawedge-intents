interface RNDataWedgeIntentsModule {
    sendBroadcastWithExtras(extras: any): void;
    registerBroadcastReceiver(filter: any): void;
}
declare const RNDataWedgeIntents: RNDataWedgeIntentsModule;
declare const DataWedgeIntents: {
    sendBroadcastWithExtras(extrasObject: any): void;
    registerBroadcastReceiver(filter: {
        filterActions: string[];
        filterCategories: string[];
    }): void;
    ACTION_SOFTSCANTRIGGER: string;
    ACTION_SCANNERINPUTPLUGIN: string;
    ACTION_ENUMERATESCANNERS: string;
    ACTION_SETDEFAULTPROFILE: string;
    ACTION_RESETDEFAULTPROFILE: string;
    ACTION_SWITCHTOPROFILE: string;
    START_SCANNING: string;
    STOP_SCANNING: string;
    TOGGLE_SCANNING: string;
    ENABLE_PLUGIN: string;
    DISABLE_PLUGIN: string;
};
export { RNDataWedgeIntents };
export default DataWedgeIntents;
//# sourceMappingURL=index.d.ts.map