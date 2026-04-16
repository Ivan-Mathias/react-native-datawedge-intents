export type Scan = {
    data: string;
    decoder: string;
    timeAtDecode: Date;
};
export type DataWedgeIntent = {
    'com.symbol.datawedge.data_string'?: string;
    'com.symbol.datawedge.label_type'?: string;
    [key: string]: unknown;
};
export interface DataWedgeConfig {
    packageName: string;
    profileName?: string;
    disableKeystroke?: boolean;
}
declare const DataWedgeService: {
    initialize: ({ packageName, profileName, disableKeystroke }: DataWedgeConfig) => void;
    /**
     * Switch the scanner light (torch) on or off.
     * @param {boolean} flashOn
     */
    setIllumination: (flashOn: boolean) => void;
    /**
     * Helper to parse the raw intent into a clean object.
     * @param {object} intent
     */
    parseIntentToScan: (intent: any) => Scan | null;
};
export default DataWedgeService;
//# sourceMappingURL=DataWedgeService.d.ts.map