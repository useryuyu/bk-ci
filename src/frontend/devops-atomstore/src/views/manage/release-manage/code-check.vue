<template>
    <article class="code-check">
        <code-check></code-check>
    </article>
</template>

<script>
    import codeCheck from '@/components/code-check'

    export default {
        components: {
            codeCheck
        }
    }
</script>

<style lang="scss" scoped>
    .code-check {
        height: 100%;
        flex: 1;
        background: #fff;
    }
</style>
