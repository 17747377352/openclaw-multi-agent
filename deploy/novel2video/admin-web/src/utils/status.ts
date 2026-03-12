export type BadgeTone = 'info' | 'success' | 'warning' | 'danger' | 'neutral';

export function projectStatusMeta(status: number) {
  if (status === 3) return { label: '已完成', tone: 'success' as BadgeTone };
  if (status === 2) return { label: '分镜处理中', tone: 'info' as BadgeTone };
  if (status === 1) return { label: '进行中', tone: 'warning' as BadgeTone };
  return { label: '待开始', tone: 'neutral' as BadgeTone };
}

export function reviewStatusMeta(status: string) {
  if (status === 'approved') return { label: '已确认', tone: 'success' as BadgeTone };
  if (status === 'generating') return { label: '生成中', tone: 'info' as BadgeTone };
  if (status === 'rejected') return { label: '已拒绝', tone: 'danger' as BadgeTone };
  return { label: '待处理', tone: 'warning' as BadgeTone };
}

export function videoStatusMeta(status: string) {
  if (status === 'completed') return { label: '已完成', tone: 'success' as BadgeTone };
  if (status === 'processing') return { label: '生成中', tone: 'info' as BadgeTone };
  if (status === 'failed') return { label: '失败', tone: 'danger' as BadgeTone };
  return { label: '等待中', tone: 'warning' as BadgeTone };
}
