export type ThreadStatus =
  | "WAITING_USER"
  | "WAITING_ADMIN"
  | "RESOLVED";

export type ThreadFilterStatus =
  | "OPEN"
  | "WAITING_USER"
  | "WAITING_ADMIN"
  | "RESOLVED";

export type UserType = "USER" | "ADMIN";

export type ThreadType = "BUG" | "PERFORMANCE" |  "UX" | "SUGGESTION";
  
export interface ThreadItem {
  id: number;
  title: string;
  status: ThreadStatus;
  type: ThreadType;
  lastMessageAt: string;
  createdAt: string;
  unreadByUser: number;
  unreadByAdmin: number;
}

export interface ThreadPageResp {
  items: ThreadItem[];
  nextCursor: string | null;
  hasMore: boolean;
}

export interface GetThreadsParams {
  status?: string;
  size?: number;
  cursor?: string | null;
}

export type ThreadMessageDto = {
    id: number;
    threadId: number;
    userType: UserType;
    senderId: number;
    body: string;
    isInternal: boolean;
    createdAt: string;
  };

  export type MessagePageResp = {
    items: ThreadMessageDto[];
    nextCursor: string | null;
    hasMore: boolean;
  };