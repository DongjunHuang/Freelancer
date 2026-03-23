<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { createThread } from '@/api/issue'

import {
  NCard,
  NTabs,
  NTabPane,
  NForm,
  NFormItem,
  NInput,
  NSelect,
  NButton,
  NDivider,
  type FormInst,
  type FormRules,
} from 'naive-ui'

const activeTab = ref<'issue'>('issue')

const issueFormRef = ref<FormInst | null>(null)
const message = useMessage()
const issueModel = reactive({
  title: '',
  description: '',
  type: '',
  impact: '',
})

// The types of the issues
const issueTypeOptions = [
  { label: 'Bug', value: 'BUG' },
  { label: 'Performance', value: 'PERFORMANCE' },
  { label: 'UX', value: 'UX' },
  { label: 'Suggestion', value: 'SUGGESTION' },
]

// Check rules
const issueRules: FormRules = {
  title: [
    { required: true, message: 'Please enter a title', trigger: ['input', 'blur'] },
    { min: 6, message: 'Title should be at least 6 characters', trigger: ['input', 'blur'] },
  ],
  description: [
    { required: true, message: 'Please provide descriptions.', trigger: ['input', 'blur'] },
  ],
}

function resetIssue() {
  Object.assign(issueModel, {
    title: '',
    type: 'bug',
    url: '',
    impact: '',
  })
}

const onSubmitIssue = () => {
  issueFormRef.value?.validate(async (errors) => {
    if (errors) return

    try {
      const res = await createThread(
        issueModel.title,
        issueModel.description,
        issueModel.impact,
        issueModel.type,
      )
      message.success('Submit successful.')
      resetIssue()
    } catch (err) {
      message.error('Submit failed')
    }
  })
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-6 py-8">
    <NCard class="shadow-sm" content-style="padding: 0;">
      <div class="p-6">
        <div class="flex items-start justify-between gap-6 flex-wrap">
          <div>
            <div class="text-2xl font-semibold">Feedback</div>
            <div class="mt-1 text-gray-500">
              Create an issue report or send a product suggestion.
            </div>
          </div>
        </div>

        <NDivider class="my-5" />

        <NTabs v-model:value="activeTab" type="line" animated>
          <!-- Issue -->
          <NTabPane name="issue" tab="Issue">
            <NForm
              ref="issueFormRef"
              :model="issueModel"
              :rules="issueRules"
              label-placement="top"
              size="large"
            >
              <!-- First row-->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <NFormItem label="Title" path="title">
                  <NInput
                    v-model:value="issueModel.title"
                    placeholder="e.g. Chart breaks on large CSV"
                  />
                </NFormItem>
                <NFormItem label="Type" path="type">
                  <NSelect v-model:value="issueModel.type" :options="issueTypeOptions" />
                </NFormItem>
              </div>

              <!-- Second row-->
              <NFormItem label="Description" path="decription">
                <NInput
                  v-model:value="issueModel.description"
                  type="textarea"
                  :autosize="{ minRows: 4, maxRows: 10 }"
                  placeholder="Please describe what you experienced..."
                />
              </NFormItem>

              <!-- Third row-->
              <NFormItem label="Impact(Optional)" path="impact">
                <NInput
                  v-model:value="issueModel.impact"
                  type="textarea"
                  :autosize="{ minRows: 4, maxRows: 10 }"
                  placeholder="What is the impact..."
                />
              </NFormItem>

              <div class="mt-4 flex gap-3">
                <NButton type="primary" @click="onSubmitIssue">Submit Issue</NButton>
                <NButton secondary @click="resetIssue">Reset</NButton>
              </div>
            </NForm>
          </NTabPane>
        </NTabs>
      </div>
    </NCard>
  </div>
</template>
