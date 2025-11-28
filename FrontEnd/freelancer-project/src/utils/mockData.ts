import type { FetchRecordsResp} from '@/api/types'
export const mockRecords: FetchRecordsResp = {
    datasetName: "Stock Prices",
    columns: ["salary", "bonus", "volume"],
  
    datapoints: {
      AAPL: [
        {
          recordDate: "2025-01-01",
          values: { salary: 188, bonus: 190, volume: 1234000 }
        },
        {
          recordDate: "2025-01-02",
          values: { salary: 190, bonus: 191, volume: 1105300 }
        },
        {
          recordDate: "2025-01-03",
          values: { salary: 191, bonus: 187, volume: 1422000 }
        }
      ],
  
      MSFT: [
        {
          recordDate: "2025-01-01",
          values: { salary: 410, bonus: 412, volume: 890000 }
        },
        {
          recordDate: "2025-01-02",
          values: { salary: 412, bonus: 415, volume: 945000 }
        },
        {
          recordDate: "2025-01-03",
          values: { salary: 415, bonus: 413, volume: 978000 }
        }
      ],
  
      GOOG: [
        {
          recordDate: "2025-01-01",
          values: { salary: 142, bonus: 144, volume: 750000 }
        },
        {
          recordDate: "2025-01-02",
          values: { salary: 144, bonus: 146, volume: 802000 }
        },
        {
          recordDate: "2025-01-03",
          values: { salary: 146, bonus: 145, volume: 830000 }
        }
      ]
    }
  };