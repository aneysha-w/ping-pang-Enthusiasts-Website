import fs from 'fs'
import path from 'path'
import crypto from 'crypto'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const DB_FILE = path.join(__dirname, '..', 'data', 'db.json')

export function hashPassword(password) {
  return crypto.createHash('sha256').update(password).digest('hex')
}

let db = {
  users: [],
  scoreRecords: [],
  events: [],
  enrollments: [],
  matches: [],
  clubs: [],
  clubMembers: [],
  joinRequests: [],
  posts: [],
  comments: [],
  notifications: [],
  adminAccount: { id: 1, account: 'admin', password: hashPassword('admin123') },
  nextId: 1
}

export function loadDB() {
  try {
    if (fs.existsSync(DB_FILE)) {
      db = JSON.parse(fs.readFileSync(DB_FILE, 'utf-8'))
    }
  } catch (e) {
    console.error('加载数据库失败:', e.message)
  }
  return db
}

export function saveDB() {
  try {
    const dir = path.dirname(DB_FILE)
    if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true })
    fs.writeFileSync(DB_FILE, JSON.stringify(db, null, 2))
  } catch (e) {
    console.error('保存数据库失败:', e.message)
  }
}

export function getDB() {
  return db
}

export function nextId() {
  return db.nextId++
}

setInterval(() => saveDB(), 5000)
