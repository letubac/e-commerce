# 🧪 Real-time Chat System - Test & Debug Guide

## 📌 Quick Test Checklist

### 1. **Server Status Check**
```bash
# Terminal - Check if backend is running
curl -i http://localhost:8280/actuator/health

# Expected output:
# HTTP/1.1 200 OK
# {"status":"UP"}

✅ If UP → Backend is running
❌ If connection refused → Start backend: mvn spring-boot:run
```

### 2. **REST API Test**
```bash
# Get all conversations
curl -H "Authorization: Bearer YOUR_TOKEN" \
     http://localhost:8280/api/v1/chat/conversations

# Expected:
# 200 OK with array of conversations

✅ If 200 → REST API working
❌ If 401 → Invalid token
❌ If 500 → Server error (check logs)
```

### 3. **WebSocket Connection Test**

**Option A: Using WebSocket Client Online**
1. Go to: https://www.websocket.org/echo.html
2. Change URL to: `ws://localhost:8280/ws/chat?token=YOUR_JWT_TOKEN`
3. Click "Connect"
4. Expected: `CONNECTED` message

**Option B: Using Browser Console**
```javascript
// Open developer tools → Console → paste:

const token = localStorage.getItem('token');
const ws = new WebSocket(`ws://localhost:8280/ws/chat?token=${token}`);

ws.onopen = () => console.log('✅ Connected');
ws.onmessage = (e) => console.log('📨 Received:', e.data);
ws.onerror = (e) => console.error('❌ Error:', e);

// Send test message
ws.send(JSON.stringify({ type: 'PING' }));

// Expected response: { type: 'PONG' }
```

**Option C: Using wscat CLI**
```bash
# Install: npm install -g wscat

wscat -c "ws://localhost:8280/ws/chat?token=YOUR_JWT_TOKEN"

# Once connected, send:
{"type":"PING"}

# Expected response:
{"type":"PONG"}
```

---

## 🔍 Backend Testing

### Test 1: Send Message (REST API)
```bash
curl -X POST http://localhost:8280/api/v1/chat/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $(cat token.txt)" \
  -d '{
    "conversationId": 1,
    "content": "Hello from test",
    "messageType": "TEXT"
  }'

# Expected response (200 OK):
{
  "data": {
    "id": 123,
    "conversationId": 1,
    "senderId": 5,
    "senderType": "USER",
    "content": "Hello from test",
    "messageType": "TEXT",
    "isRead": false,
    "createdAt": "2026-03-08T14:30:00Z"
  }
}

✅ SUCCESS: Message created + WebSocket broadcast sent
❌ FAIL: Check server logs for error
```

### Test 2: Mark as Read
```bash
curl -X POST http://localhost:8280/api/v1/chat/conversations/1/read \
  -H "Authorization: Bearer $(cat token.txt)"

# Expected: 200 OK
✅ SUCCESS: Unread count reset + WebSocket notification sent
```

### Test 3: Get Conversations
```bash
curl http://localhost:8280/api/v1/chat/conversations \
  -H "Authorization: Bearer $(cat token.txt)"

# Should show all conversations with:
# - id, userId, adminId, subject
# - lastMessageAt, unreadCount
# - status (ACTIVE/CLOSED/PENDING)

✅ SUCCESS: All conversations loaded
❌ FAIL: Check if data exists in DB
```

### Test 4: Get Messages in Conversation
```bash
curl "http://localhost:8280/api/v1/chat/conversations/1/messages?page=0&size=10" \
  -H "Authorization: Bearer $(cat token.txt)"

# Should show messages sorted by createdAt DESC
✅ SUCCESS: Messages loaded
❌ FAIL: Check conversation ID is valid
```

---

## 🎨 Frontend Testing

### Test 1: ChatWidget Loads
```javascript
// Open browser console on http://localhost:3000/dashboard

// Check if ChatWidget component loaded
document.querySelector('[class*="chat"]') !== null

✅ TRUE: Component mounted
❌ FALSE: Check component imports
```

### Test 2: WebSocket Connects
```javascript
// In browser console, watch chat widget:

// Method 1: Check state
console.log(document.querySelector('textarea')?.value);  // Should be empty

// Method 2: Monitor WebSocket
// Open DevTools → Network → WS
// You should see: wss://localhost:3000/ws/chat (or similar)

✅ GREEN: Connected
❌ RED/X: Connection failed - check token
```

### Test 3: Send Message
```javascript
// In ChatWidget:
1. Type "Test message"
2. Click "Send"
3. Open DevTools → Network
4. Look for: POST /api/v1/chat/messages
   - Status should be 200
   - Response should have message ID

✅ Message sent + stored
❌ Check error in response
```

### Test 4: Receive Broadcast
```javascript
// In browser console, intercept WebSocket messages:

// Find WebSocket in Network tab → WS connection
// Open connection details
// Send a message from another user/tab
// Watch Messages tab for: NEW_MESSAGE type

✅ Message received in real-time
❌ Message not received - check server logs
```

---

## 🐛 Debug Mode

### Enable Verbose Logging

**Backend Logging:**
```properties
# application.properties
logging.level.root=INFO
logging.level.com.example.ecommerce.websocket=DEBUG
logging.level.org.springframework.web.socket=DEBUG
logging.level.org.springframework.messaging=DEBUG

# This shows:
# - WebSocket connection/disconnection
# - Message sending/receiving
# - Broadcasting to sessions
# - Authentication success/failure
```

**Frontend Logging:**
```javascript
// In ChatWidget.js, modify to add logs:

ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  console.log('📥 WebSocket Message:', data);  // ← ADD THIS
  
  switch (data.type) {
    case 'NEW_MESSAGE':
      console.log('✅ New message received:', data.message);
      // ...
  }
};
```

### Monitor Server Logs
```bash
# Terminal where backend is running:
# Watch for lines like:

# ✅ WebSocket connected - User: admin, Conversation: 1
# 📨 Broadcasting message to 2 sessions
# ❌ WebSocket error: Connection reset by peer
```

---

## 🔴 Common Issues & Solutions

### Issue 1: WebSocket Connection Fails Immediately

**Symptom:**
```
❌ WebSocket is closed with code 1006
❌ Connection error: unexpected reserved bit in first payload byte
```

**Causes & Solutions:**
```bash
# 1. Check if backend is running
curl http://localhost:8280/actuator/health

# 2. Check if token is valid
# Token expired? Refresh it:
# - Login again to get new token
# - Copy token to localStorage

# 3. Check CORS settings
# In WebSocketConfig.java:
# .setAllowedOrigins("http://localhost:3000")

# 4. Check if endpoint path is correct
# Should be: ws://localhost:8280/ws/chat (not /websocket/chat)

# 5. Check port forwarding (if using VPN/proxy)
# Try direct connection: ws://127.0.0.1:8280/ws/chat
```

### Issue 2: Messages Not Broadcasting

**Symptom:**
```
✅ Message sent successfully (response 200)
❌ But other users don't receive it
```

**Debug Steps:**
```javascript
// Step 1: Check broadcastMessage() was called
// Add this in ChatMessageServiceImpl:
log.info("🔊 Broadcasting message to conversation: {}", conversationId);

// Step 2: Check if WebSocket sessions exist
// Add this in WebSocketChatService:
log.info("📊 Conversation sessions: {}", 
         conversationSessions.get(conversationId) != null ? 
         conversationSessions.get(conversationId).size() : 0);

// Step 3: Check if sessions are open
// Each session should have: session.isOpen() == true
```

**Likely Cause:** No other users connected to same conversation
- Solution: Open admin dashboard in separate window/tab

### Issue 3: Typing Indicator Not Showing

**Symptom:**
```
✅ WebSocket sends TYPING event
❌ Other user doesn't see "is typing" message
```

**Solutions:**
```javascript
// 1. Verify typing message is received
ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  console.log('📨 Received message type:', data.type);  // Check if TYPING appears
};

// 2. Check if typing UI is being set
// In ChatWidget, look for:
// setIsTyping(true) → This should display UI
// But might not be implemented yet!

// 3. Add UI indicator manually
// In ChatWidget render:
{isTyping && <div>User is typing...</div>}
```

### Issue 4: Unread Count Stays Same

**Symptom:**
```
✅ Message marked as read in DB
❌ Badge number doesn't reduce
```

**Debug:**
```bash
# Check database:
SELECT id, unread_count FROM conversations WHERE id = 1;

# If unread_count = 0 in DB:
# Problem is on frontend - check dispatch action
# dispatch({ type: ACTIONS.RESET_UNREAD })

# If unread_count > 0 in DB:
# Problem is in markAsRead() endpoint
# Check: conversationRepository.save() call
```

---

## 🧮 Performance Testing

### Load Test: Multiple Messages

```javascript
// Send 100 messages rapidly
async function stressTest() {
  for (let i = 0; i < 100; i++) {
    await fetch('/api/v1/chat/messages', {
      method: 'POST',
      body: JSON.stringify({
        conversationId: 1,
        content: `Message ${i}`,
        messageType: 'TEXT'
      })
    });
    console.log(`Sent message ${i+1}/100`);
  }
}

stressTest();

// Monitor:
// - DevTools Performance tab (should stay < 50ms per message)
// - Network traffic (messages/sec)
// - Browser memory (should not spike)
```

### Load Test: Multiple Users

```javascript
// Open 5 browser tabs all on same conversation
// Have each tab send messages at same time
// Monitor:
// - All tabs receive all messages
// - No messages missing
// - No duplicate messages
// - Latency < 500ms
```

---

## ✅ Verification Checklist

- [ ] Backend runs without errors: `mvn spring-boot:run`
- [ ] Frontend runs: `npm start`
- [ ] Can login and get token
- [ ] REST API responds (conversations list)
- [ ] WebSocket connects (no errors in console)
- [ ] Send message appears after POST
- [ ] Message appears in other tab/user instantly (WebSocket)
- [ ] Typing indicator works (other user sees)
- [ ] Mark as read reduces unread count
- [ ] No error messages in server logs
- [ ] No error messages in browser console
- [ ] Messages persist after page refresh
- [ ] Database shows correct data

---

## 📊 Expected Metrics

| Metric | Target | Current |
|--------|--------|---------|
| WebSocket connection time | < 500ms | ? |
| Message delivery latency | < 100ms | ? |
| Typing indicator latency | < 200ms | ? |
| Broadcast to 10 sessions | < 50ms | ? |
| API response time | < 200ms | ? |
| Database query time | < 100ms | ? |

**How to measure:**
```javascript
// In browser console:
const start = Date.now();
// ... perform action ...
const elapsed = Date.now() - start;
console.log(`⏱️ Took ${elapsed}ms`);
```

---

## 🎓 Test Scenarios

### Scenario 1: Basic Chat Flow (5 minutes)
```
1. ✅ Open http://localhost:3000 (customer)
2. ✅ Open http://localhost:3000/admin (admin login)
3. ✅ Customer selects conversation
4. ✅ Customer sends message
5. ✅ Admin should see message instantly
6. ✅ Admin replies
7. ✅ Customer should see reply instantly
8. ✅ Exit and reopen - message should still be there
```

### Scenario 2: Real-time Indicators (3 minutes)
```
1. ✅ Open both customer and admin
2. ✅ Customer starts typing
3. ✅ Admin should see "Customer is typing..." (if implemented)
4. ✅ Customer stops typing for 3 seconds
5. ✅ "is typing" message should disappear
6. ✅ Admin reads message
7. ✅ Customer should see checkmark (if implemented)
```

### Scenario 3: Multiple Conversations (5 minutes)
```
1. ✅ Open conversation #1
2. ✅ Open conversation #2 in new tab
3. ✅ Send message in tab #1
4. ✅ Only tab #1 should update (not tab #2)
5. ✅ Switch to tab #2, send message
6. ✅ Only tab #2 should update
7. ✅ Verify database has both messages
```

### Scenario 4: Network Interruption (5 minutes)
```
1. ✅ Open WebSocket connection
2. ✅ Stop backend (Ctrl+C)
3. ✅ Frontend should fallback to polling
4. ✅ Send message - should still work (via REST)
5. ✅ Start backend again
6. ✅ WebSocket should reconnect
7. ✅ Verify no messages lost
```

---

## 📱 Browser DevTools Tips

### Network Tab Monitoring
```
1. Open DevTools → Network
2. Filter: ws (show WebSocket only)
3. Send a message
4. Watch WebSocket traffic:
   - Click on WS connection
   - Messages tab shows real-time messages
   - Each message should show:
     * Type: NEW_MESSAGE / TYPING / READ
     * Size: < 1KB per message
     * Time: < 50ms
```

### Console Tips
```javascript
// Search for WebSocket logs:
// Filter: "WebSocket|socket|ws:" (case-insensitive)

// Check for errors:
// Filter: "error|Error|ERROR|❌"

// Check for timing:
// Search: "Sent|Received|Connected|Disconnected"

// Monitor specific events:
const ws = websocketRef.current;
ws.addEventListener('message', (e) => {
  const data = JSON.parse(e.data);
  if (data.type === 'NEW_MESSAGE') {
    console.log('⚡ Message received in:', Date.now(), 'ms');
  }
});
```

### Storage Tab
```
1. Open DevTools → Application → LocalStorage
2. Find "token" key
3. Copy value (this is your JWT)
4. Use it in curl commands for testing
5. Verify token expiration (iat, exp claims in JWT payload)
```

---

## 🚨 When It's Not Working

**Step 1: Check Logs**
```bash
# Terminal running backend - look for:
# ERROR | WARN in red/yellow text

# Most common errors:
# - "Unauthorized" → Token invalid
# - "Conversation not found" → Wrong ID
# - "Connection refused" → Port 8280 blocked
# - "NullPointerException" → Missing field in request
```

**Step 2: Check Network**
```bash
# Verify ports are accessible
lsof -i :8280      # Check if backend port open
lsof -i :3000      # Check if frontend port open

# Test connectivity
curl http://localhost:8280/actuator/health
curl http://localhost:3000          # Should return HTML
```

**Step 3: Clear Cache**
```bash
# Browser cache
# DevTools → Application → Clear Storage

# Rebuild frontend
cd e-commerce-fe
rm -rf node_modules/.cache
npm start

# Clear localStorage
localStorage.clear()

# Get new token by logging in again
```

**Step 4: Check Database**
```bash
# Connect to PostgreSQL
psql -U user -d ecommerce_db

# Check chat tables
SELECT COUNT(*) FROM conversations;
SELECT COUNT(*) FROM chat_messages;

# Check recent messages
SELECT * FROM chat_messages ORDER BY created_at DESC LIMIT 5;
```

---

## 📞 Getting Help

**If still not working after these steps:**

1. **Check recent logs:** `tail -50 ~/logs/app.log`
2. **Restart everything:**
   ```bash
   # Kill processes
   pkill -f "spring-boot"
   pkill -f "npm start"
   
   # Clear data (optional)
   # DELETE FROM chat_messages; DELETE FROM conversations;
   
   # Restart
   mvn spring-boot:run
   npm start
   ```
3. **Check configuration:**
   - Verify `application.properties` has correct DB credentials
   - Verify `REACT_APP_API_URL` in frontend `.env`
   - Verify CORS settings in WebSocketConfig

---

*Last Updated: 2026-03-08*  
*For debugging help, check server logs and browser console first!*

