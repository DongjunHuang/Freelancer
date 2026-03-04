<script setup lang="ts">
import hero from "@/assets/images/hero.webp";
</script>

<template>
  <section
    class="relative h-screen min-h-[420px] overflow-hidden"
    :style="{
      backgroundImage: `url(${hero})`,
      backgroundSize: '120%',
      backgroundPosition: 'center',
      backgroundRepeat: 'no-repeat'
    }">
    <div
      class="absolute inset-0 bg-gradient-to-r from-black/80 via-black/50 to-black/10"></div>

    <div
      class="relative z-10 h-full flex flex-col justify-center px-10 md:px-16 text-white max-w-3xl">
      <h1 class="text-4xl md:text-5xl font-semibold tracking-wide leading-tight">
        Engineering. Systems. Discipline.
      </h1>

      <p class="mt-4 text-white/80 text-lg">
        Building long-term scalable software systems.
      </p>

      <div class="mt-8 flex gap-4">
        <button
          class="px-6 py-3 bg-blue-600 hover:bg-blue-500 rounded-xl font-medium transition">
          Get Started
        </button>

        <button
          class="px-6 py-3 border border-white/40 hover:bg-white/10 rounded-xl transition">
          Learn More
        </button>
      </div>
    </div>
  </section>
</template>