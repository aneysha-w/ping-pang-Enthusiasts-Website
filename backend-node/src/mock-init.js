import { getDB, saveDB, nextId, hashPassword } from './db.js'

function now() { return new Date().toISOString() }
function daysFromNow(d) { return new Date(Date.now() + d * 86400000).toISOString() }
function hoursFromNow(h) { return new Date(Date.now() + h * 3600000).toISOString() }

const CITIES = ['北京', '上海', '广州', '深圳']
const LEVELS = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'PROFESSIONAL']
const LEVEL_NAMES = ['业余初级', '业余中级', '业余高级', '专业级']

const MOCK_USERS = [
  { nickname: '张小明', realName: '张明', gender: 'MALE', city: '北京', skillLevel: 'BEGINNER', score: 850 },
  { nickname: '李大伟', realName: '李伟', gender: 'MALE', city: '上海', skillLevel: 'BEGINNER', score: 920 },
  { nickname: '王芳', realName: '王芳', gender: 'FEMALE', city: '广州', skillLevel: 'BEGINNER', score: 780 },
  { nickname: '赵磊', realName: '赵磊', gender: 'MALE', city: '深圳', skillLevel: 'BEGINNER', score: 980 },
  { nickname: '陈静', realName: '陈静', gender: 'FEMALE', city: '北京', skillLevel: 'INTERMEDIATE', score: 1100 },
  { nickname: '杨帆', realName: '杨帆', gender: 'MALE', city: '上海', skillLevel: 'INTERMEDIATE', score: 1250 },
  { nickname: '刘洋', realName: '刘洋', gender: 'MALE', city: '广州', skillLevel: 'INTERMEDIATE', score: 1050 },
  { nickname: '周婷', realName: '周婷', gender: 'FEMALE', city: '深圳', skillLevel: 'INTERMEDIATE', score: 1180 },
  { nickname: '吴强', realName: '吴强', gender: 'MALE', city: '北京', skillLevel: 'ADVANCED', score: 1450 },
  { nickname: '郑爽', realName: '郑爽', gender: 'FEMALE', city: '上海', skillLevel: 'ADVANCED', score: 1600 },
  { nickname: '孙浩', realName: '孙浩', gender: 'MALE', city: '广州', skillLevel: 'ADVANCED', score: 1380 },
  { nickname: '马琳', realName: '马琳', gender: 'FEMALE', city: '深圳', skillLevel: 'ADVANCED', score: 1520 },
  { nickname: '朱国', realName: '朱国', gender: 'MALE', city: '北京', skillLevel: 'PROFESSIONAL', score: 1850 },
  { nickname: '胡丽', realName: '胡丽', gender: 'FEMALE', city: '上海', skillLevel: 'PROFESSIONAL', score: 2100 },
  { nickname: '林峰', realName: '林峰', gender: 'MALE', city: '广州', skillLevel: 'PROFESSIONAL', score: 1950 },
]

const MOCK_CLUBS = [
  { name: '北京乒乓健将俱乐部', city: '北京', description: '北京地区资深乒乓球爱好者交流平台，定期举办友谊赛和积分赛。', creatorIdx: 8 },
  { name: '上海旋风乒乓社', city: '上海', description: '上海最具活力的乒乓球社团，欢迎各水平球友加入，每周三、六活动。', creatorIdx: 9 },
  { name: '广州粤球俱乐部', city: '广州', description: '广州本土乒乓球俱乐部，以球会友，切磋技艺，共同进步。', creatorIdx: 10 },
  { name: '深圳鹏城乒乓汇', city: '深圳', description: '深圳乒乓球爱好者聚集地，专业教练指导，赛事丰富。', creatorIdx: 11 },
]

export function initMockDataIfNeeded() {
  const db = getDB()
  if (db.users && db.users.length > 0) {
    console.log('[MockInit] 数据库已有数据，跳过初始化')
    return false
  }

  console.log('[MockInit] 开始写入 Mock 数据...')

  try {
    const userIds = []
    for (let i = 0; i < MOCK_USERS.length; i++) {
      const u = MOCK_USERS[i]
      const user = {
        id: nextId(), phone: `138${String(10000000 + i).padStart(8, '0')}`, password: hashPassword('123456'),
        nickname: u.nickname, realName: u.realName, gender: u.gender,
        city: u.city, skillLevel: u.skillLevel, avatarUrl: null,
        currentScore: u.score, eventScore: Math.floor(u.score * 0.8), friendlyScore: Math.floor(u.score * 0.2),
        matchCount: Math.floor(Math.random() * 30) + 5, lastMatchTime: daysFromNow(-Math.floor(Math.random() * 30) - 1),
        registerTime: daysFromNow(-60), lastLoginTime: hoursFromNow(-Math.floor(Math.random() * 48)),
        profileCompleted: true, role: 'USER'
      }
      db.users.push(user)
      userIds.push(user.id)
    }

    const clubIds = []
    for (let i = 0; i < MOCK_CLUBS.length; i++) {
      const c = MOCK_CLUBS[i]
      const creatorId = userIds[c.creatorIdx]
      const club = {
        id: nextId(), name: c.name, creatorId, city: c.city,
        description: c.description, logoUrl: null,
        memberCount: 1, status: 'ACTIVE', createTime: daysFromNow(-30)
      }
      db.clubs.push(club)
      clubIds.push(club.id)
      db.clubMembers.push({
        id: nextId(), clubId: club.id, userId: creatorId,
        role: 'ADMIN', joinTime: daysFromNow(-30), status: 'ACTIVE'
      })

      const memberIndices = []
      for (let j = 0; j < MOCK_USERS.length; j++) {
        if (j === c.creatorIdx) continue
        if (MOCK_USERS[j].city === c.city && Math.random() > 0.4) {
          memberIndices.push(j)
        }
      }
      for (const mIdx of memberIndices.slice(0, 4)) {
        db.clubMembers.push({
          id: nextId(), clubId: club.id, userId: userIds[mIdx],
          role: 'MEMBER', joinTime: daysFromNow(-Math.floor(Math.random() * 20) - 1), status: 'ACTIVE'
        })
        club.memberCount++
      }

      const postTitles = ['欢迎加入我们的俱乐部！', '本周训练计划分享', '上次比赛心得交流']
      for (let p = 0; p < 2 + Math.floor(Math.random() * 2); p++) {
        db.posts.push({
          id: nextId(), clubId: club.id, authorId: creatorId,
          title: postTitles[p % postTitles.length],
          content: '欢迎大家积极参与俱乐部活动，一起提高球技，享受乒乓球带来的乐趣！',
          commentCount: Math.floor(Math.random() * 5), publishTime: daysFromNow(-Math.floor(Math.random() * 15) - 1),
          editTime: null, status: 'ACTIVE', formerMemberPost: false
        })
      }
    }

    const events = [
      { name: '2024春季北京公开赛', level: 'OPEN', format: 'KNOCKOUT', maxPlayers: 8, status: 'FINISHED', city: '北京', clubId: null, startOffset: -10, enrollOffset: -12 },
      { name: '上海俱乐部友谊赛', level: 'FRIENDLY', format: 'ROUND_ROBIN', maxPlayers: 4, status: 'FINISHED', city: '上海', clubId: null, startOffset: -7, enrollOffset: -9 },
      { name: '广州夏季锦标赛', level: 'CHAMPIONSHIP', format: 'KNOCKOUT', maxPlayers: 8, status: 'IN_PROGRESS', city: '广州', clubId: null, startOffset: -1, enrollOffset: -3 },
      { name: '深圳周末俱乐部赛', level: 'CLUB', format: 'ROUND_ROBIN', maxPlayers: 4, status: 'IN_PROGRESS', city: '深圳', clubId: null, startOffset: 0, enrollOffset: -2 },
      { name: '北京秋季邀请赛', level: 'OPEN', format: 'KNOCKOUT', maxPlayers: 16, status: 'PENDING', city: '北京', clubId: null, startOffset: 7, enrollOffset: 5 },
      { name: '全国业余锦标赛预选赛', level: 'CHAMPIONSHIP', format: 'KNOCKOUT', maxPlayers: 32, status: 'PENDING', city: '上海', clubId: null, startOffset: 14, enrollOffset: 12 },
      { name: '广州友谊交流赛', level: 'FRIENDLY', format: 'ROUND_ROBIN', maxPlayers: 8, status: 'PENDING', city: '广州', clubId: null, startOffset: 5, enrollOffset: 3 },
    ]

    for (const e of events) {
      const organizerId = userIds[Math.floor(Math.random() * userIds.length)]
      const event = {
        id: nextId(), name: e.name, level: e.level, format: e.format,
        maxPlayers: e.maxPlayers, enrolledCount: 0,
        startTime: daysFromNow(e.startOffset), enrollDeadline: daysFromNow(e.enrollOffset),
        location: e.city + '体育馆', status: e.status,
        organizerId, clubId: e.clubId
      }

      if (e.status === 'FINISHED' || e.status === 'IN_PROGRESS') {
        const enrollCount = Math.min(e.maxPlayers, 4 + Math.floor(Math.random() * 4))
        const enrolledUserIds = []
        for (let i = 0; i < enrollCount; i++) {
          const uid = userIds[i % userIds.length]
          if (!enrolledUserIds.includes(uid)) {
            enrolledUserIds.push(uid)
            db.enrollments.push({
              id: nextId(), eventId: event.id, userId: uid,
              enrollTime: daysFromNow(e.enrollOffset + 1), status: 'ENROLLED'
            })
            event.enrolledCount++
          }
        }

        if (e.status === 'FINISHED') {
          for (let i = 0; i < enrolledUserIds.length - 1; i += 2) {
            const playerA = enrolledUserIds[i]
            const playerB = enrolledUserIds[i + 1]
            const winnerId = Math.random() > 0.5 ? playerA : playerB
            const match = {
              id: nextId(), eventId: event.id, round: Math.floor(i / 2) + 1,
              playerA, playerB, winnerId, status: 'FINISHED', isBye: false
            }
            db.matches.push(match)

            const winner = db.users.find(u => u.id === winnerId)
            const loserId = winnerId === playerA ? playerB : playerA
            const loser = db.users.find(u => u.id === loserId)
            const k = 32 * ({ CHAMPIONSHIP: 1.5, OPEN: 1.2, CLUB: 1.0, FRIENDLY: 0.5 }[e.level] || 1.0)
            const expected = 1.0 / (1.0 + Math.pow(10, (loser.currentScore - winner.currentScore) / 400.0))
            const change = Math.round(k * (1.0 - expected))
            const winnerBefore = winner.currentScore
            const loserBefore = loser.currentScore
            winner.currentScore = winnerBefore + change
            loser.currentScore = loserBefore - change
            winner.matchCount++; loser.matchCount++
            winner.lastMatchTime = event.startTime; loser.lastMatchTime = event.startTime
            winner.eventScore += change; loser.eventScore -= change
            db.scoreRecords.push({ id: nextId(), userId: winnerId, eventId: event.id, matchId: match.id, scoreChange: change, scoreBefore: winnerBefore, scoreAfter: winner.currentScore, changeType: 'EVENT', createTime: event.startTime })
            db.scoreRecords.push({ id: nextId(), userId: loserId, eventId: event.id, matchId: match.id, scoreChange: -change, scoreBefore: loserBefore, scoreAfter: loser.currentScore, changeType: 'EVENT', createTime: event.startTime })
          }
        } else {
          for (let i = 0; i < enrolledUserIds.length - 1; i += 2) {
            db.matches.push({
              id: nextId(), eventId: event.id, round: Math.floor(i / 2) + 1,
              playerA: enrolledUserIds[i], playerB: enrolledUserIds[i + 1],
              winnerId: null, status: 'PENDING', isBye: false
            })
          }
        }
      } else if (e.status === 'PENDING') {
        const enrollCount = Math.min(e.maxPlayers - 1, 2 + Math.floor(Math.random() * 4))
        for (let i = 0; i < enrollCount; i++) {
          const uid = userIds[i % userIds.length]
          const exists = db.enrollments.find(en => en.eventId === event.id && en.userId === uid)
          if (!exists) {
            db.enrollments.push({
              id: nextId(), eventId: event.id, userId: uid,
              enrollTime: hoursFromNow(-Math.floor(Math.random() * 24)), status: 'ENROLLED'
            })
            event.enrolledCount++
          }
        }
      }

      db.events.push(event)
    }

    const pendingRequests = [
      { applicantIdx: 0, clubIdx: 1 },
      { applicantIdx: 1, clubIdx: 2 },
      { applicantIdx: 2, clubIdx: 3 },
      { applicantIdx: 3, clubIdx: 0 },
    ]
    for (const pr of pendingRequests) {
      const applicantId = userIds[pr.applicantIdx]
      const clubId = clubIds[pr.clubIdx]
      const alreadyMember = db.clubMembers.find(m => m.clubId === clubId && m.userId === applicantId && m.status === 'ACTIVE')
      if (!alreadyMember) {
        db.joinRequests.push({
          id: nextId(), clubId, applicantId, status: 'PENDING',
          applyTime: hoursFromNow(-Math.floor(Math.random() * 48) - 1),
          approveTime: null, approverId: null, rejectReason: null
        })
      }
    }

    saveDB()

    console.log('[MockInit] Mock 数据写入完成！')
    console.log(`  - 用户: ${db.users.length} 个`)
    console.log(`  - 赛事: ${db.events.length} 场`)
    console.log(`  - 俱乐部: ${db.clubs.length} 个`)
    console.log(`  - 帖子: ${db.posts.length} 篇`)
    console.log(`  - 比赛记录: ${db.matches.length} 场`)
    console.log(`  - 积分记录: ${db.scoreRecords.length} 条`)
    console.log(`  - 待审批申请: ${db.joinRequests.filter(r => r.status === 'PENDING').length} 个`)
    console.log('  - 测试用户密码统一为: 123456')
    return true
  } catch (e) {
    console.error('[MockInit] 数据初始化失败:', e.message)
    return false
  }
}