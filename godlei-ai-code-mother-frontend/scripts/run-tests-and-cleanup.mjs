import { readdir, rm, stat } from 'node:fs/promises'
import { join, resolve } from 'node:path'
import { spawn } from 'node:child_process'

const projectRoot = resolve(import.meta.dirname, '..')
const ignoredDirNames = new Set(['node_modules', 'dist', '.git'])
const removableDirNames = new Set(['__tests__', 'test', 'tests'])
const removableFilePattern = /\.(test|spec)\.[cm]?[jt]sx?$/

const filePathsToDelete = []
const dirPathsToDelete = []

const collectTargets = async (currentDir) => {
  const entries = await readdir(currentDir, { withFileTypes: true })

  for (const entry of entries) {
    const fullPath = join(currentDir, entry.name)

    if (entry.isDirectory()) {
      if (ignoredDirNames.has(entry.name)) {
        continue
      }
      if (removableDirNames.has(entry.name)) {
        dirPathsToDelete.push(fullPath)
        continue
      }
      await collectTargets(fullPath)
      continue
    }

    if (entry.isFile() && removableFilePattern.test(entry.name)) {
      filePathsToDelete.push(fullPath)
    }
  }
}

const deleteCollectedTargets = async () => {
  await collectTargets(projectRoot)

  for (const filePath of filePathsToDelete) {
    await rm(filePath, { force: true })
  }

  dirPathsToDelete.sort((left, right) => right.length - left.length)
  for (const dirPath of dirPathsToDelete) {
    await rm(dirPath, { recursive: true, force: true })
  }
}

const removeEmptyParentDirs = async (startDir) => {
  let currentDir = startDir

  while (currentDir.startsWith(projectRoot) && currentDir !== projectRoot) {
    const entries = await readdir(currentDir)
    if (entries.length > 0) {
      return
    }
    await rm(currentDir, { recursive: true, force: true })
    currentDir = resolve(currentDir, '..')
  }
}

const cleanupTests = async () => {
  await deleteCollectedTargets()
  const scriptsDir = resolve(projectRoot, 'scripts')
  const scriptsStat = await stat(scriptsDir).catch(() => null)
  if (scriptsStat?.isDirectory()) {
    await removeEmptyParentDirs(scriptsDir)
  }
}

const runVitest = () =>
  new Promise((resolveExitCode, reject) => {
    const vitestEntry = resolve(projectRoot, 'node_modules', 'vitest', 'vitest.mjs')

    const child = spawn(process.execPath, [vitestEntry, 'run', '--passWithNoTests'], {
      cwd: projectRoot,
      stdio: 'inherit',
      shell: false,
    })

    child.on('error', reject)
    child.on('close', (code) => resolveExitCode(code ?? 1))
  })

const main = async () => {
  let exitCode = 1

  try {
    exitCode = await runVitest()
  } finally {
    await cleanupTests()
  }

  process.exit(exitCode)
}

await main()
