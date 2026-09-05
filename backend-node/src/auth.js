import { getDB, hashPassword } from './db.js'

const sessions = {}
const adminSessions = {}
const smsCodes = {}
const smsLimits = {}

export function createUserToken(userId) {
  const token = Math.random().toString(36).substring(2) + Date.now().toString(36)
  sessions[token] = userId
  return token
}

export function getUserId(token) {
  if (!token) return null
  return sessions[token] || null
}

export function createAdminToken(adminId) {
  const token = 'admin_' + Math.random().toString(36).substring(2) + Date.now().toString(36)
  adminSessions[token] = adminId
  return token
}

export function getAdminId(token) {
  if (!token) return null
  return adminSessions[token] || null
}

export function saveSmsCode(phone, code) {
  smsCodes[phone] = code
}

export function getSmsCode(phone) {
  return smsCodes[phone] || null
}

export function removeSmsCode(phone) {
  delete smsCodes[phone]
}

export function checkSmsLimit(phone) {
  const now = Date.now()
  if (smsLimits[phone] && now - smsLimits[phone] < 60000) return false
  smsLimits[phone] = now
  return true
}

export function authMiddleware(req, res, next) {
  const token = (req.headers.authorization || '').replace('Bearer ', '')
  const userId = getUserId(token)
  if (!userId) return res.json({ code: 90002, message: '未登录或登录已过期' })
  req.userId = userId
  next()
}

export function adminAuthMiddleware(req, res, next) {
  const token = (req.headers.authorization || '').replace('Bearer ', '')
  const adminId = getAdminId(token)
  if (!adminId) return res.json({ code: 403, message: '无管理后台权限' })
  req.adminId = adminId
  next()
}