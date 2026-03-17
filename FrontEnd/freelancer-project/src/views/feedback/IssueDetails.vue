<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { useRoute } from "vue-router"
import { ThreadStatus } from "@/types/thread";
import { getThread, getThreadMessages, postThreadMessage, updateThreadStatus} from "@/api/issue";
import type { ThreadItem, ThreadMessageDto } from "@/types/thread";
import {
  NCard,
  NTag,
  NButton,
  NInput,
  NDivider,
  NSpin,
  useMessage,
  type SelectOption
} from "naive-ui";

const route = useRoute()
const thread = ref<ThreadItem | null>(null);
const messages = ref<ThreadMessageDto[]>([]);

const nextCursor = ref<string | null>(null);
const hasMore = ref(false);

const canReply = ref(true);


async function fetchPage() {
  try {
    const threadId = Number(route.params.id);
    console.log("Thread id is {}", threadId)

    const threadResp = await getThread(threadId);
    thread.value = threadResp.data;

    const msgResp = await getThreadMessages(threadId, { size: 20 });

    messages.value = msgResp.data.items;
    nextCursor.value = msgResp.data.nextCursor;
    hasMore.value = msgResp.data.hasMore;
  } finally {
  }
}

/*
async function loadOlderMessages() {
  if (!hasMore.value || !nextCursor.value) return;

  const threadId = Number(route.params.threadId);

  const resp = await getThreadMessages(threadId, {
    size: 20,
    cursor: nextCursor.value
  });

  messages.value = [...resp.items, ...messages.value];

  nextCursor.value = resp.nextCursor;
  hasMore.value = resp.hasMore;
}*/

onMounted( () => {
    fetchPage()  
    console.log("Page loaded");
  }
)


// ---- actions ----
async function submitReply() {
  const content = reply.content.trim();
  if (!content) {
    message.warning("Please enter your reply.");
    return;
  }
  if (!canReply.value) {
    message.warning("This issue is resolved. Re-open it to reply.");
    return;
  }

  sending.value = true;
  try {
    if (thread.value) {
      await postThreadMessage(thread.value.id, content);
      const threadResp = await getThread(thread.value.id);
      const messageResp = await getThreadMessages(thread.value.id, { size: 20 })
    

      thread.value = threadResp.data;
      messages.value = messageResp.data.items;
      nextCursor.value = messageResp.data.nextCursor;
      hasMore.value = messageResp.data.hasMore;
    } 
  } finally {
    sending.value = false;
    reply.content = ''
  }
}

function fmtTime(iso: string) {
  const d = new Date(iso);
  return d.toLocaleString();
}

async function changeStatus(status: ThreadStatus) {
  if (!thread.value) return;

  try {
    await updateThreadStatus(thread.value.id, status);

    const resp = await getThread(thread.value.id);
    thread.value = resp.data;
  } catch (e) {
    console.error("failed to update status", e);
  }
}

// ============================
/*
async function fetchMessages() {
  loading.value = true;

  try {
    const resp = await getThreadMessages(threadId, {
      size: 20,
      cursor: null,
    });

    messages.value = resp.items;
    nextCursor.value = resp.nextCursor;
    hasMore.value = resp.hasMore;
  } finally {
    loading.value = false;
  }
}*/




const message = useMessage();

const issueId = computed(() => String(route.params.id));

const sending = ref(false);
const updatingStatus = ref(false);


const reply = reactive({
  content: ""
});
</script>

<template>
  <div class="max-w-5xl mx-auto px-6 py-8">
      <NCard v-if="thread" class="shadow-sm">
        <!-- header -->
        <div class="flex items-start justify-between gap-6 flex-wrap">
          <div class="min-w-0">
            <div class="flex items-center gap-3 flex-wrap">
              <div class="text-2xl font-semibold truncate">
                {{ thread.title }}
              </div>
              <NTag type="info" size="small">{{ thread.status?.toUpperCase() }}</NTag>
              <NTag type="info" size="small">{{ thread.type?.toUpperCase() }}</NTag>
            </div>
            <div class="mt-2 text-gray-500 text-sm">
              Issue #{{ thread.id }} · Updated {{ fmtTime(thread.lastMessageAt) }}
            </div>
          </div>
        </div>

        <NDivider class="my-5" />

        <!-- messages -->
        <div class="space-y-4">
          <div
            v-for="m in messages"
            :key="m.id"
            class="flex"
            :class="m.userType === 'USER' ? 'justify-end' : 'justify-start'">
            <div
              class="max-w-[820px] rounded-2xl px-4 py-3 border"
              :class="m.userType === 'USER'
                ? 'bg-blue-600/10 border-blue-600/20'
                : 'bg-gray-50 border-gray-200'">
              <div class="flex items-center gap-2 mb-1">
                <span class="text-xs font-medium"
                  :class="m.userType === 'USER' ? 'text-blue-700' : 'text-gray-700'">
                  {{ m.userType === 'USER' ? 'You' : 'Admin' }}
                </span>
                <span class="text-xs text-gray-400">{{ fmtTime(m.createdAt) }}</span>
              </div>
              <div class="whitespace-pre-wrap text-sm text-gray-900">
                {{ m.body }}
              </div>
            </div>
          </div>
        </div>

        <NDivider class="my-6" />

        <!-- reply -->
        <div class="space-y-3">
          <div class="flex items-center justify-between">
            <div class="font-medium">Reply</div>
            <div v-if="!canReply" class="text-sm text-gray-500">
              This issue is resolved. Switch status to <span class="font-medium">Open</span> to reply.
            </div>
          </div>

          <NInput
            v-model:value="reply.content"
            type="textarea"
            :autosize="{ minRows: 4, maxRows: 10 }"
            placeholder="Write your reply..."
            :disabled="!canReply || sending"/>

          <div class="flex gap-3">
            <NButton type="primary" :loading="sending" :disabled="!canReply" @click="submitReply">
              Send Reply
            </NButton>

            <NButton
              secondary
              :disabled="updatingStatus || thread.status === 'RESOLVED'"
              @click="changeStatus('RESOLVED')">
              Mark Resolved
            </NButton>
          </div>
        </div>
      </NCard>
  </div>
</template>