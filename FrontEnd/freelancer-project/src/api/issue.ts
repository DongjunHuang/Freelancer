import http from './http'
import type { ThreadStatus, ThreadItem, GetThreadsParams, ThreadPageResp, MessagePageResp} from "@/types/thread";

export const getThreads = (params: GetThreadsParams) =>
  http.get<ThreadPageResp>(`/issues/getThreads`, {
    params: {
      status: params.status,
      size: params.size,
      cursor: params.cursor ?? undefined
    }
  });

export const createIssueThread = (
    title: string,
    description: string,
    impact: string | null,
    type: string
  ) =>
    http.post(`/issues/createThread`, {
      title,
      description,
      impact,
      type
    })

export const postThreadMessage = (threadId: number, body: string) =>
      http.post(`/issues/${threadId}/messages`, {
        body
      });


export type GetThreadMessagesParams = {
    size?: number;
};
      
export const getThreadMessages = (
    threadId: number,
    params: GetThreadMessagesParams = {}) =>
        http.get<MessagePageResp>(`/issues/${threadId}/messages`, {
            params: {
        size: params.size ?? 20
    }
});


export const getThread = (threadId: number) =>
  http.get<ThreadItem>(`/issues/${threadId}`);

export const updateThreadStatus = (threadId: number, status: ThreadStatus) =>
  http.patch(`/issues/${threadId}/status`, {
    status
  });