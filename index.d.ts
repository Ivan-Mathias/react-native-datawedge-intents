declare module 'react-native-datawedge-intents' {
  export interface ExtrasObject {
    action?: string
    extras?: { [key: string]: any }
  }

  export interface FilterObject {
    filterActions: string[]
    filterCategories: string[]
  }

  const DataWedgeIntents: {
    ACTION_SOFTSCANTRIGGER: string
    ACTION_SCANNERINPUTPLUGIN: string
    ACTION_ENUMERATESCANNERS: string
    ACTION_SETDEFAULTPROFILE: string
    ACTION_RESETDEFAULTPROFILE: string
    ACTION_SWITCHTOPROFILE: string
    START_SCANNING: string
    STOP_SCANNING: string
    TOGGLE_SCANNING: string
    ENABLE_PLUGIN: string
    DISABLE_PLUGIN: string

    sendBroadcastWithExtras(extrasObject: ExtrasObject): void
    registerBroadcastReceiver(filter: FilterObject): void
  }

  export default DataWedgeIntents

  export interface Scan {
    data: string
    decoder: string
    timeAtDecode: Date
  }

  export interface DataWedgeConfig {
    packageName: string
    profileName?: string
    disableKeystroke?: boolean
  }

  export const DataWedgeService: {
    initialize(config: DataWedgeConfig): void
    setIllumination(flashOn: boolean): void
    parseIntentToScan(intent: any): Scan | null
  }
}
