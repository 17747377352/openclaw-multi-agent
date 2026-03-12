import { createRouter, createWebHashHistory } from 'vue-router';

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      redirect: '/projects'
    },
    {
      path: '/projects',
      name: 'projects',
      component: () => import('@/views/ProjectListView.vue')
    },
    {
      path: '/characters',
      name: 'characters',
      component: () => import('@/views/CharacterReviewView.vue')
    },
    {
      path: '/storyboards',
      name: 'storyboards',
      component: () => import('@/views/StoryboardReviewView.vue')
    },
    {
      path: '/videos',
      name: 'videos',
      component: () => import('@/views/VideoTasksView.vue')
    }
  ]
});

export default router;
