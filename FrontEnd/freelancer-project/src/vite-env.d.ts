interface ImportMetaEnv {
    readonly VITE_API_BASE: string
    // 其它自定义变量……
  }
  interface ImportMeta {
    readonly env: ImportMetaEnv
  }