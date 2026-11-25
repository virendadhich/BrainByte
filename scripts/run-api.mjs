import { spawn } from 'node:child_process'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const projectRoot = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const mavenWrapper = resolve(
  projectRoot,
  'backend',
  process.platform === 'win32' ? 'mvnw.cmd' : 'mvnw',
)

const command = process.platform === 'win32' ? 'cmd.exe' : 'sh'
const args = process.platform === 'win32'
  ? ['/d', '/s', '/c', 'backend\\mvnw.cmd -f backend/pom.xml spring-boot:run']
  : [mavenWrapper, '-f', 'backend/pom.xml', 'spring-boot:run']

const processHandle = spawn(command, args, {
  cwd: projectRoot,
  stdio: 'inherit',
})

processHandle.on('error', (error) => {
  console.error(`Unable to start the Java API: ${error.message}`)
  process.exitCode = 1
})

processHandle.on('exit', (code) => {
  process.exitCode = code ?? 1
})
