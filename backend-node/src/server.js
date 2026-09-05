import express from 'express'
import cors from 'cors'
import { loadDB, saveDB, getDB, nextId, hashPassword } from './db.js'
import { createUserToken, getUserId, createAdminToken, getAdminId, saveSmsCode, getSmsCode, removeSmsCode, checkSmsLimit, authMiddleware, adminAuthMiddleware } from './auth.js'
import { initMockDataIfNeeded } from './mock-init.js'

const app = express()
app.use(cors())
app.use(express.json())

const PORT = 8080
loadDB()

function success(data) { return { code: 0, message: 'success', data } }
function error(code, message) { return { code, message } }
function now() { return new Date().toISOString() }

const SENSITIVE_WORDS = ['垃圾', '废物', '傻逼', '操', 'fuck', 'shit']
function containsSensitive(text) {
  if (!text) return false
  return SENSITIVE_WORDS.some(w => text.toLowerCase().includes(w.toLowerCase()))
}

function getLevelWeight(level) {
  return { CHAMPIONSHIP: 1.5, OPEN: 1.2, CLUB: 1.0, FRIENDLY: 0.5 }[level] || 1.0
}

function calculateEloChange(scoreA, scoreB, aWins, eventLevel) {
  const k = 32 * getLevelWeight(eventLevel)
  const expectedA = 1.0 / (1.0 + Math.pow(10, (scoreB - scoreA) / 400.0))
  const changeA = Math.round(k * ((aWins ? 1.0 : 0.0) - expectedA))
  const changeB = -changeA
  return [changeA, changeB]
}

function sendNotification(receiverId, type, title, content, relatedBusinessId) {
  const db = getDB()
  db.notifications.push({
    id: nextId(), receiverId, type, title, content,
    isRead: false, relatedBusinessId: relatedBusinessId || null,
    createTime: now()
  })
}

// ==================== 用户管理 ====================

app.post('/users/sms-code', (req, res) => {
  const { phone } = req.body
  if (!phone || !/^1[3-9]\d{9}$/.test(phone)) return res.json(error(90001, '手机号格式非法'))
  if (!checkSmsLimit(phone)) return res.json(error(40004, '发送频次超限，请1分钟后再试'))
  const code = String(Math.floor(Math.random() * 1000000)).padStart(6, '0')
  saveSmsCode(phone, code)
  res.json(success({ code }))
})

app.post('/users/register', (req, res) => {
  const { phone, password, smsCode } = req.body
  const db = getDB()
  if (db.users.find(u => u.phone === phone)) return res.json(error(40001, '手机号已注册'))
  if (getSmsCode(phone) !== smsCode) return res.json(error(40002, '验证码错误或过期'))
  removeSmsCode(phone)
  const user = {
    id: nextId(), phone, password: hashPassword(password),
    nickname: '球友' + phone.slice(-4), realName: null, gender: null,
    city: null, skillLevel: null, avatarUrl: null,
    currentScore: 1000, eventScore: 0, friendlyScore: 0, matchCount: 0,
    lastMatchTime: null, registerTime: now(), lastLoginTime: null,
    profileCompleted: false, role: 'USER'
  }
  db.users.push(user)
  saveDB()
  res.json(success({ token: createUserToken(user.id) }))
})

app.post('/users/login', (req, res) => {
  const { phone, password } = req.body
  const db = getDB()
  const user = db.users.find(u => u.phone === phone)
  if (!user) return res.json(error(40005, '账号不存在'))
  if (user.password !== hashPassword(password)) return res.json(error(40006, '密码错误'))
  user.lastLoginTime = now()
  saveDB()
  res.json(success({ token: createUserToken(user.id) }))
})

app.get('/users/me', authMiddleware, (req, res) => {
  const db = getDB()
  const user = db.users.find(u => u.id === req.userId)
  if (!user) return res.json(error(40010, '用户不存在'))
  res.json(success(user))
})

app.get('/users/:userId', (req, res) => {
  const db = getDB()
  const user = db.users.find(u => u.id === parseInt(req.params.userId))
  if (!user) return res.json(error(40010, '用户不存在'))
  res.json(success(user))
})

app.put('/users/:userId/profile', authMiddleware, (req, res) => {
  if (req.userId !== parseInt(req.params.userId)) return res.json(error(403, '无权操作'))
  const db = getDB()
  const user = db.users.find(u => u.id === req.userId)
  if (!user) return res.json(error(40010, '用户不存在'))
  const { nickname, realName, gender, city, skillLevel } = req.body
  if (nickname && containsSensitive(nickname)) return res.json(error(40008, '昵称包含违规内容'))
  if (skillLevel && !['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'PROFESSIONAL'].includes(skillLevel))
    return res.json(error(40007, '技术水平等级非法'))
  if (nickname !== undefined) user.nickname = nickname
  if (realName !== undefined) user.realName = realName
  if (gender !== undefined) user.gender = gender
  if (city !== undefined) user.city = city
  if (skillLevel !== undefined) user.skillLevel = skillLevel
  user.profileCompleted = true
  saveDB()
  res.json(success(user))
})

app.post('/users/:userId/avatar', authMiddleware, (req, res) => {
  if (req.userId !== parseInt(req.params.userId)) return res.json(error(403, '无权操作'))
  const db = getDB()
  const user = db.users.find(u => u.id === req.userId)
  if (!user) return res.json(error(40010, '用户不存在'))
  user.avatarUrl = req.body.avatarUrl
  saveDB()
  res.json(success({ avatarUrl: user.avatarUrl }))
})

app.get('/users/nearby', (req, res) => {
  const db = getDB()
  const { city } = req.query
  let users = [...db.users]
  if (city) users = users.filter(u => u.city === city)
  users.sort((a, b) => b.currentScore - a.currentScore)
  res.json(success(users))
})

// ==================== 积分排名 ====================

app.get('/scores/:userId/history', (req, res) => {
  const db = getDB()
  const records = db.scoreRecords.filter(r => r.userId === parseInt(req.params.userId))
    .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  res.json(success(records))
})

app.get('/scores/ranking/global', (req, res) => {
  const db = getDB()
  const users = [...db.users].sort((a, b) => {
    if (b.currentScore !== a.currentScore) return b.currentScore - a.currentScore
    return new Date(b.lastMatchTime || 0) - new Date(a.lastMatchTime || 0)
  })
  res.json(success(users))
})

app.get('/scores/ranking/clubs/:clubId', (req, res) => {
  const db = getDB()
  const clubId = parseInt(req.params.clubId)
  const memberUserIds = db.clubMembers.filter(m => m.clubId === clubId && m.status === 'ACTIVE').map(m => m.userId)
  const users = db.users.filter(u => memberUserIds.includes(u.id))
    .sort((a, b) => b.currentScore - a.currentScore)
  res.json(success(users))
})

app.get('/scores/ranking/by-level', (req, res) => {
  const { skillLevel, scope, clubId } = req.query
  if (!['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'PROFESSIONAL'].includes(skillLevel))
    return res.json(error(50003, '技术水平等级非法'))
  const db = getDB()
  let users = db.users.filter(u => u.skillLevel === skillLevel)
  if (scope === 'CLUB' && clubId) {
    const memberUserIds = db.clubMembers.filter(m => m.clubId === parseInt(clubId) && m.status === 'ACTIVE').map(m => m.userId)
    users = users.filter(u => memberUserIds.includes(u.id))
  }
  users.sort((a, b) => {
    if (b.currentScore !== a.currentScore) return b.currentScore - a.currentScore
    return new Date(b.lastMatchTime || 0) - new Date(a.lastMatchTime || 0)
  })
  res.json(success(users))
})

app.get('/scores/ranking/grouped-by-level', (req, res) => {
  const db = getDB()
  const levels = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'PROFESSIONAL']
  const result = {}
  for (const level of levels) {
    result[level] = db.users.filter(u => u.skillLevel === level)
      .sort((a, b) => b.currentScore - a.currentScore)
  }
  res.json(success(result))
})

// ==================== 赛事管理 ====================

app.post('/events', authMiddleware, (req, res) => {
  const { name, level, format, maxPlayers, startTime, enrollDeadline, location, clubId } = req.body
  if (!name || !level || !format || !maxPlayers || !startTime || !enrollDeadline || !location)
    return res.json(error(60003, '赛事字段不完整'))
  if (maxPlayers < 4 || maxPlayers > 256) return res.json(error(60003, '赛事人数需在4-256之间'))
  if (format === 'KNOCKOUT' && (maxPlayers & (maxPlayers - 1)) !== 0)
    return res.json(error(60004, '淘汰赛人数需为2的幂次方'))
  const db = getDB()
  const event = {
    id: nextId(), name, level, format, maxPlayers, enrolledCount: 0,
    startTime, enrollDeadline, location, status: 'PENDING',
    organizerId: req.userId, clubId: clubId || null
  }
  db.events.push(event)
  saveDB()
  res.json(success(event))
})

app.get('/events', (req, res) => {
  const db = getDB()
  let events = [...db.events].sort((a, b) => new Date(b.startTime) - new Date(a.startTime))
  if (req.query.status) events = events.filter(e => e.status === req.query.status)
  if (req.query.level) events = events.filter(e => e.level === req.query.level)
  res.json(success(events))
})

app.get('/events/:eventId', (req, res) => {
  const db = getDB()
  const event = db.events.find(e => e.id === parseInt(req.params.eventId))
  if (!event) return res.json(error(60003, '赛事不存在'))
  res.json(success(event))
})

app.post('/events/:eventId/enroll', authMiddleware, (req, res) => {
  const db = getDB()
  const event = db.events.find(e => e.id === parseInt(req.params.eventId))
  if (!event) return res.json(error(60003, '赛事不存在'))
  const user = db.users.find(u => u.id === req.userId)
  if (!user || !user.profileCompleted) return res.json(error(60006, '请先完善个人资料'))
  if (new Date() > new Date(event.enrollDeadline)) return res.json(error(60008, '报名已截止'))
  if (event.enrolledCount >= event.maxPlayers) return res.json(error(60001, '名额已满'))
  if (db.enrollments.find(e => e.eventId === event.id && e.userId === req.userId))
    return res.json(error(60001, '已报名该赛事'))
  if (event.clubId) {
    const isMember = db.clubMembers.find(m => m.clubId === event.clubId && m.userId === req.userId && m.status === 'ACTIVE')
    if (!isMember) return res.json(error(60007, '仅限本俱乐部成员报名'))
  }
  db.enrollments.push({ id: nextId(), eventId: event.id, userId: req.userId, enrollTime: now(), status: 'ENROLLED' })
  event.enrolledCount++
  sendNotification(req.userId, 'EVENT_ENROLL', '报名成功', `您已成功报名赛事: ${event.name}`, event.id)
  saveDB()
  res.json(success(null))
})

app.delete('/events/:eventId/enroll', authMiddleware, (req, res) => {
  const db = getDB()
  const event = db.events.find(e => e.id === parseInt(req.params.eventId))
  if (!event) return res.json(error(60003, '赛事不存在'))
  if (new Date() > new Date(event.enrollDeadline)) return res.json(error(60008, '报名已截止，无法取消'))
  const enrollment = db.enrollments.find(e => e.eventId === event.id && e.userId === req.userId)
  if (!enrollment) return res.json(error(60009, '未报名该赛事'))
  db.enrollments = db.enrollments.filter(e => e !== enrollment)
  event.enrolledCount = Math.max(0, event.enrolledCount - 1)
  saveDB()
  res.json(success(null))
})

app.get('/events/:eventId/enrollments', (req, res) => {
  const db = getDB()
  res.json(success(db.enrollments.filter(e => e.eventId === parseInt(req.params.eventId))))
})

app.post('/events/:eventId/schedule', authMiddleware, (req, res) => {
  const db = getDB()
  const event = db.events.find(e => e.id === parseInt(req.params.eventId))
  if (!event) return res.json(error(60003, '赛事不存在'))
  if (event.status !== 'PENDING') return res.json(error(60010, '赛事状态不允许生成赛程'))
  const enrollments = db.enrollments.filter(e => e.eventId === event.id)
  if (enrollments.length < 4) return res.json(error(60002, '报名人数不足'))

  let playerIds = enrollments.map(e => e.userId)
  let players = db.users.filter(u => playerIds.includes(u.id))
  players.sort((a, b) => b.currentScore - a.currentScore)
  let sortedIds = players.map(u => u.id)

  const matches = []
  if (event.format === 'KNOCKOUT') {
    let n = sortedIds.length
    let nextPow2 = 1
    while (nextPow2 < n) nextPow2 *= 2
    let bracket = [...sortedIds, ...Array(nextPow2 - n).fill(null)]
    for (let i = 0; i < nextPow2; i += 2) {
      const m = { id: nextId(), eventId: event.id, round: 1, playerA: bracket[i], playerB: bracket[i + 1], winnerId: null, status: 'PENDING', isBye: false }
      if (m.playerB === null) { m.isBye = true; m.status = 'FINISHED'; m.winnerId = m.playerA }
      matches.push(m)
    }
  } else {
    let ids = [...sortedIds]
    let n = ids.length
    if (n % 2 !== 0) { ids.push(null); n++ }
    let rounds = n - 1
    for (let r = 0; r < rounds; r++) {
      for (let i = 0; i < n / 2; i++) {
        let a = (r + i) % (n - 1)
        let b = (n - 1 - i + r) % (n - 1)
        let playerA = i === 0 ? ids[n - 1] : ids[a]
        let playerB = i === 0 ? ids[a] : ids[b]
        if (playerA !== null && playerB !== null)
          matches.push({ id: nextId(), eventId: event.id, round: r + 1, playerA, playerB, winnerId: null, status: 'PENDING', isBye: false })
      }
    }
  }
  db.matches.push(...matches)
  event.status = 'IN_PROGRESS'
  saveDB()
  res.json(success(matches))
})

app.get('/events/:eventId/matches', (req, res) => {
  const db = getDB()
  res.json(success(db.matches.filter(m => m.eventId === parseInt(req.params.eventId)).sort((a, b) => a.round - b.round)))
})

app.post('/events/:eventId/matches/:matchId/result', authMiddleware, (req, res) => {
  const db = getDB()
  const event = db.events.find(e => e.id === parseInt(req.params.eventId))
  if (!event) return res.json(error(60003, '赛事不存在'))
  if (event.status !== 'IN_PROGRESS') return res.json(error(50001, '赛事状态非法'))
  if (event.organizerId !== req.userId) return res.json(error(50002, '无权录入比赛结果'))
  const match = db.matches.find(m => m.id === parseInt(req.params.matchId))
  if (!match || match.status === 'FINISHED') return res.json(success(null))

  const winnerId = parseInt(req.body.winnerId)
  const loserId = match.playerA === winnerId ? match.playerB : match.playerA
  const winner = db.users.find(u => u.id === winnerId)
  const loser = db.users.find(u => u.id === loserId)

  const [winnerChange, loserChange] = calculateEloChange(winner.currentScore, loser.currentScore, true, event.level)
  const winnerBefore = winner.currentScore
  const loserBefore = loser.currentScore

  winner.currentScore = winnerBefore + winnerChange
  loser.currentScore = loserBefore + loserChange
  winner.matchCount++; loser.matchCount++
  winner.lastMatchTime = now(); loser.lastMatchTime = now()
  winner.eventScore += winnerChange; loser.eventScore += loserChange

  db.scoreRecords.push({ id: nextId(), userId: winnerId, eventId: event.id, matchId: match.id, scoreChange: winnerChange, scoreBefore: winnerBefore, scoreAfter: winner.currentScore, changeType: 'EVENT', createTime: now() })
  db.scoreRecords.push({ id: nextId(), userId: loserId, eventId: event.id, matchId: match.id, scoreChange: loserChange, scoreBefore: loserBefore, scoreAfter: loser.currentScore, changeType: 'EVENT', createTime: now() })

  match.winnerId = winnerId
  match.status = 'FINISHED'
  sendNotification(winnerId, 'SCORE_CHANGE', '积分变动', `您在赛事${event.name}中获胜，积分已更新`, event.id)
  sendNotification(loserId, 'SCORE_CHANGE', '积分变动', `您在赛事${event.name}中失利，积分已更新`, event.id)
  saveDB()
  res.json(success(null))
})

app.put('/events/:eventId/status', authMiddleware, (req, res) => {
  const db = getDB()
  const event = db.events.find(e => e.id === parseInt(req.params.eventId))
  if (!event) return res.json(error(60003, '赛事不存在'))
  const newStatus = req.body.status
  const valid = (event.status === 'PENDING' && newStatus === 'IN_PROGRESS') ||
    (event.status === 'IN_PROGRESS' && newStatus === 'FINISHED') ||
    (event.status === 'PENDING' && newStatus === 'CANCELLED')
  if (!valid) return res.json(error(60011, '非法状态流转'))
  event.status = newStatus
  if (newStatus === 'CANCELLED') {
    db.enrollments.filter(e => e.eventId === event.id).forEach(e =>
      sendNotification(e.userId, 'EVENT_CANCEL', '赛事取消', `赛事${event.name}已取消`, event.id))
  }
  saveDB()
  res.json(success(null))
})

app.delete('/events/:eventId', authMiddleware, (req, res) => {
  const db = getDB()
  const event = db.events.find(e => e.id === parseInt(req.params.eventId))
  if (!event) return res.json(error(60003, '赛事不存在'))
  if (event.status !== 'PENDING') return res.json(error(60012, '赛事已开始，不可取消'))
  event.status = 'CANCELLED'
  db.enrollments.filter(e => e.eventId === event.id).forEach(e =>
    sendNotification(e.userId, 'EVENT_CANCEL', '赛事取消', `赛事${event.name}已取消`, event.id))
  saveDB()
  res.json(success(null))
})

// ==================== 俱乐部 ====================

app.post('/clubs', authMiddleware, (req, res) => {
  const { name, city, description, logoUrl } = req.body
  const db = getDB()
  const user = db.users.find(u => u.id === req.userId)
  if (!user || !user.profileCompleted) return res.json(error(40007, '请先完善个人资料'))
  if (db.clubs.find(c => c.name === name)) return res.json(error(70001, '俱乐部名称已存在'))
  const club = { id: nextId(), name, creatorId: req.userId, city, description, logoUrl, memberCount: 1, status: 'ACTIVE', createTime: now() }
  db.clubs.push(club)
  db.clubMembers.push({ id: nextId(), clubId: club.id, userId: req.userId, role: 'ADMIN', joinTime: now(), status: 'ACTIVE' })
  saveDB()
  res.json(success(club))
})

app.get('/clubs', (req, res) => {
  const db = getDB()
  let clubs = db.clubs.filter(c => c.status === 'ACTIVE')
  if (req.query.city) clubs = clubs.filter(c => c.city === req.query.city)
  res.json(success(clubs))
})

app.get('/clubs/nearby', (req, res) => {
  const db = getDB()
  let clubs = db.clubs.filter(c => c.status === 'ACTIVE')
  if (req.query.city) clubs = clubs.filter(c => c.city === req.query.city)
  res.json(success(clubs))
})

app.get('/clubs/:clubId', (req, res) => {
  const db = getDB()
  const club = db.clubs.find(c => c.id === parseInt(req.params.clubId))
  if (!club) return res.json(error(40010, '俱乐部不存在'))
  res.json(success(club))
})

app.get('/clubs/:clubId/members', (req, res) => {
  const db = getDB()
  res.json(success(db.clubMembers.filter(m => m.clubId === parseInt(req.params.clubId) && m.status === 'ACTIVE')))
})

app.post('/clubs/:clubId/join-requests', authMiddleware, (req, res) => {
  const db = getDB()
  const clubId = parseInt(req.params.clubId)
  if (db.clubMembers.find(m => m.clubId === clubId && m.userId === req.userId && m.status === 'ACTIVE'))
    return res.json(error(70002, '已是俱乐部成员'))
  if (db.joinRequests.find(r => r.clubId === clubId && r.applicantId === req.userId && r.status === 'PENDING'))
    return res.json(error(70002, '已有待审核申请'))
  const req_ = { id: nextId(), clubId, applicantId: req.userId, status: 'PENDING', applyTime: now(), approveTime: null, approverId: null, rejectReason: null }
  db.joinRequests.push(req_)
  const club = db.clubs.find(c => c.id === clubId)
  const admins = db.clubMembers.filter(m => m.clubId === clubId && m.role === 'ADMIN' && m.status === 'ACTIVE')
  admins.forEach(a => sendNotification(a.userId, 'CLUB_JOIN_REQUEST', '新的加入申请', `有新用户申请加入俱乐部: ${club.name}`, req_.id))
  saveDB()
  res.json(success(req_))
})

app.post('/clubs/:clubId/posts', authMiddleware, (req, res) => {
  const db = getDB()
  const clubId = parseInt(req.params.clubId)
  if (!db.clubMembers.find(m => m.clubId === clubId && m.userId === req.userId && m.status === 'ACTIVE'))
    return res.json(error(70002, '请先加入俱乐部'))
  const { title, content } = req.body
  if (containsSensitive(title) || containsSensitive(content)) return res.json(error(70003, '内容包含违规信息'))
  const post = { id: nextId(), clubId, authorId: req.userId, title, content, commentCount: 0, publishTime: now(), editTime: null, status: 'ACTIVE', formerMemberPost: false }
  db.posts.push(post)
  saveDB()
  res.json(success(post))
})

app.get('/clubs/:clubId/posts', (req, res) => {
  const db = getDB()
  res.json(success(db.posts.filter(p => p.clubId === parseInt(req.params.clubId) && p.status === 'ACTIVE').sort((a, b) => new Date(b.publishTime) - new Date(a.publishTime))))
})

app.delete('/clubs/:clubId/posts/:postId', authMiddleware, (req, res) => {
  const db = getDB()
  const post = db.posts.find(p => p.id === parseInt(req.params.postId))
  if (!post) return res.json(error(40010, '帖子不存在'))
  const clubId = parseInt(req.params.clubId)
  const isAdmin = db.clubMembers.find(m => m.clubId === clubId && m.userId === req.userId && m.role === 'ADMIN' && m.status === 'ACTIVE')
  if (post.authorId !== req.userId && !isAdmin) return res.json(error(70004, '无权删除帖子'))
  post.status = 'DELETED'
  saveDB()
  res.json(success(null))
})

app.post('/clubs/:clubId/posts/:postId/comments', authMiddleware, (req, res) => {
  const db = getDB()
  const clubId = parseInt(req.params.clubId)
  if (!db.clubMembers.find(m => m.clubId === clubId && m.userId === req.userId && m.status === 'ACTIVE'))
    return res.json(error(70002, '请先加入俱乐部'))
  if (containsSensitive(req.body.content)) return res.json(error(70003, '内容包含违规信息'))
  const comment = { id: nextId(), postId: parseInt(req.params.postId), authorId: req.userId, content: req.body.content, createTime: now(), status: 'ACTIVE' }
  db.comments.push(comment)
  const post = db.posts.find(p => p.id === parseInt(req.params.postId))
  if (post) post.commentCount++
  saveDB()
  res.json(success(comment))
})

app.get('/clubs/posts/:postId/comments', (req, res) => {
  const db = getDB()
  res.json(success(db.comments.filter(c => c.postId === parseInt(req.params.postId) && c.status === 'ACTIVE').sort((a, b) => new Date(a.createTime) - new Date(b.createTime))))
})

// ==================== 管理后台 ====================

app.post('/admin/login', (req, res) => {
  const { account, password } = req.body
  const db = getDB()
  if (db.adminAccount.account !== account) return res.json(error(80003, '非系统管理员账号'))
  if (db.adminAccount.password !== hashPassword(password)) return res.json(error(80001, '账号或密码错误'))
  res.json(success({ token: createAdminToken(db.adminAccount.id) }))
})

app.post('/admin/events', adminAuthMiddleware, (req, res) => {
  const { name, level, format, maxPlayers, startTime, enrollDeadline, location } = req.body
  if (!name || !level || !format || !maxPlayers || !startTime || !enrollDeadline || !location)
    return res.json(error(60003, '赛事字段不完整'))
  if (maxPlayers < 4 || maxPlayers > 256) return res.json(error(60003, '赛事人数需在4-256之间'))
  if (format === 'KNOCKOUT' && (maxPlayers & (maxPlayers - 1)) !== 0)
    return res.json(error(60004, '淘汰赛人数需为2的幂次方'))
  if (new Date(enrollDeadline) >= new Date(startTime))
    return res.json(error(60005, '报名截止时间需早于开始时间'))
  const db = getDB()
  const event = {
    id: nextId(), name, level, format, maxPlayers, enrolledCount: 0,
    startTime, enrollDeadline, location, status: 'PENDING',
    organizerId: null, clubId: null
  }
  db.events.push(event)
  saveDB()
  res.json(success({ eventId: event.id }))
})

app.get('/admin/events', adminAuthMiddleware, (req, res) => {
  const db = getDB()
  let events = [...db.events].sort((a, b) => new Date(b.startTime) - new Date(a.startTime))
  if (req.query.status) events = events.filter(e => e.status === req.query.status)
  res.json(success(events))
})

app.post('/admin/clubs', adminAuthMiddleware, (req, res) => {
  const { name, city, description } = req.body
  if (!name || name.length < 2 || name.length > 30)
    return res.json(error(70001, '俱乐部名称需2-30个字符'))
  if (!city) return res.json(error(70001, '城市为必填项'))
  const db = getDB()
  if (db.clubs.find(c => c.name === name)) return res.json(error(70001, '俱乐部名称已存在'))
  const club = {
    id: nextId(), name, creatorId: null, city,
    description: description || '', logoUrl: null,
    memberCount: 0, status: 'ACTIVE', createTime: now()
  }
  db.clubs.push(club)
  saveDB()
  res.json(success({ clubId: club.id }))
})

app.get('/admin/clubs', adminAuthMiddleware, (req, res) => {
  const db = getDB()
  let clubs = [...db.clubs].sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  res.json(success(clubs))
})

app.get('/admin/join-requests', adminAuthMiddleware, (req, res) => {
  const db = getDB()
  let requests = [...db.joinRequests].sort((a, b) => new Date(b.applyTime) - new Date(a.applyTime))
  if (req.query.status) requests = requests.filter(r => r.status === req.query.status)
  res.json(success(requests))
})

app.put('/admin/join-requests/:requestId', adminAuthMiddleware, (req, res) => {
  const db = getDB()
  const req_ = db.joinRequests.find(r => r.id === parseInt(req.params.requestId))
  if (!req_) return res.json(error(80004, '申请不存在'))
  if (req_.status !== 'PENDING') return res.json(error(80002, '该申请已处理'))
  const { approved, rejectReason } = req.body
  req_.approverId = req.adminId
  req_.approveTime = now()
  const club = db.clubs.find(c => c.id === req_.clubId)
  if (approved) {
    req_.status = 'APPROVED'
    db.clubMembers.push({ id: nextId(), clubId: req_.clubId, userId: req_.applicantId, role: 'MEMBER', joinTime: now(), status: 'ACTIVE' })
    if (club) club.memberCount++
    sendNotification(req_.applicantId, 'CLUB_JOIN_AUDIT', '加入申请已通过', `您加入俱乐部${club?.name}的申请已通过`, req_.clubId)
  } else {
    req_.status = 'REJECTED'
    req_.rejectReason = rejectReason || null
    sendNotification(req_.applicantId, 'CLUB_JOIN_AUDIT', '加入申请已拒绝', `您加入俱乐部${club?.name}的申请已被拒绝${rejectReason ? '，原因: ' + rejectReason : ''}`, req_.clubId)
  }
  saveDB()
  res.json(success(null))
})

// ==================== 首页 & 通知 ====================

app.get('/home', authMiddleware, (req, res) => {
  const db = getDB()
  const user = db.users.find(u => u.id === req.userId)
  const globalRanking = [...db.users].sort((a, b) => b.currentScore - a.currentScore)
  const myRank = globalRanking.findIndex(u => u.id === req.userId) + 1
  const recentEvents = db.events.sort((a, b) => new Date(b.startTime) - new Date(a.startTime)).slice(0, 5)
  let nearbyClubs = db.clubs.filter(c => c.status === 'ACTIVE')
  if (user.city) nearbyClubs = nearbyClubs.filter(c => c.city === user.city)
  res.json(success({
    profile: { userId: user.id, nickname: user.nickname, avatarUrl: user.avatarUrl, currentScore: user.currentScore, city: user.city },
    myRank: myRank || -1,
    rankingPreview: globalRanking.slice(0, 10).map(u => ({ userId: u.id, nickname: u.nickname, currentScore: u.currentScore })),
    recentEvents: recentEvents.map(e => ({ eventId: e.id, name: e.name, level: e.level, status: e.status, startTime: e.startTime })),
    nearbyClubs: nearbyClubs.slice(0, 5).map(c => ({ clubId: c.id, name: c.name, city: c.city, memberCount: c.memberCount }))
  }))
})

app.get('/notifications', authMiddleware, (req, res) => {
  const db = getDB()
  res.json(success(db.notifications.filter(n => n.receiverId === req.userId).sort((a, b) => new Date(b.createTime) - new Date(a.createTime))))
})

// ==================== 错误处理 ====================

app.use((err, req, res, next) => {
  console.error('服务器错误:', err.stack || err.message || err)
  res.status(500).json({ code: 90000, message: '服务器内部错误: ' + (err.message || '未知错误') })
})

// ==================== 启动 ====================

app.listen(PORT, () => {
  console.log(`后端服务已启动: http://localhost:${PORT}`)
  console.log(`管理员账号: admin / admin123`)
  initMockDataIfNeeded()
})