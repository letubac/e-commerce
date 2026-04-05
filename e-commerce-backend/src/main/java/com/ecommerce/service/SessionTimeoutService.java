package com.ecommerce.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.time.ZoneOffset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
/**
 * author: LeTuBac
 */
public class SessionTimeoutService {

	@Value("${app.session.timeout:1800000}") // 30 phút mặc định
	private long sessionTimeoutMs;

	@Value("${app.jwt.expiration:3600000}") // 1 giờ mặc định
	private long jwtExpirationMs;

	// Lưu trữ thông tin session của người dùng
	private final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();

	public static class SessionInfo {
		private final Date loginTime;
		private Date lastActivity;
		private final String userId;
		private final String sessionId;

		public SessionInfo(String userId, String sessionId) {
			this.userId = userId;
			this.sessionId = sessionId;
			this.loginTime = new Date();
			this.lastActivity = new Date();
		}

		public void updateActivity() {
			this.lastActivity = new Date();
		}

		// Getters
		public Date getLoginTime() {
			return loginTime;
		}

		public Date getLastActivity() {
			return lastActivity;
		}

		public String getUserId() {
			return userId;
		}

		public String getSessionId() {
			return sessionId;
		}
	}

	/**
	 * Đăng ký session mới cho người dùng
	 */
	public void registerUserSession(String userId, String sessionId) {
		SessionInfo sessionInfo = new SessionInfo(userId, sessionId);
		activeSessions.put(sessionId, sessionInfo);
	}

	/**
	 * Cập nhật hoạt động cuối cùng của người dùng
	 */
	public void updateUserActivity(String sessionId) {
		SessionInfo session = activeSessions.get(sessionId);
		if (session != null) {
			session.updateActivity();
		}
	}

	/**
	 * Kiểm tra xem session có còn hiệu lực không
	 */
	public boolean isSessionValid(String sessionId) {
		SessionInfo session = activeSessions.get(sessionId);
		if (session == null) {
			return false;
		}

		Date now = new Date();
		long inactiveTimeMs = now.getTime() - session.getLastActivity().getTime();

		return inactiveTimeMs < sessionTimeoutMs;
	}

	/**
	 * Lấy thời gian còn lại của session (tính bằng giây)
	 */
	public long getSessionTimeRemaining(String sessionId) {
		SessionInfo session = activeSessions.get(sessionId);
		if (session == null) {
			return 0;
		}

		Date now = new Date();
		long inactiveTimeMs = now.getTime() - session.getLastActivity().getTime();

		long remainingMs = sessionTimeoutMs - inactiveTimeMs;
		return remainingMs > 0 ? remainingMs / 1000 : 0;
	}

	/**
	 * Kiểm tra xem có nên cảnh báo timeout không (còn 5 phút)
	 */
	public boolean shouldWarnTimeout(String sessionId) {
		long remainingSeconds = getSessionTimeRemaining(sessionId);
		return remainingSeconds > 0 && remainingSeconds <= 300; // 5 phút = 300 giây
	}

	/**
	 * Hủy session của người dùng
	 */
	public void invalidateUserSession(String sessionId) {
		activeSessions.remove(sessionId);
	}

	/**
	 * Làm sạch các session đã hết hạn
	 */
	public void cleanupExpiredSessions() {
		Date now = new Date();
		activeSessions.entrySet().removeIf(entry -> {
			SessionInfo session = entry.getValue();
			long inactiveTimeMs = now.getTime() - session.getLastActivity().getTime();
			return inactiveTimeMs >= sessionTimeoutMs;
		});
	}

	/**
	 * Lấy số lượng session đang hoạt động
	 */
	public int getActiveSessionCount() {
		return activeSessions.size();
	}

	/**
	 * Lấy timeout configuration
	 */
	public Map<String, Long> getTimeoutConfig() {
		Map<String, Long> config = new ConcurrentHashMap<>();
		config.put("sessionTimeoutMs", sessionTimeoutMs);
		config.put("jwtExpirationMs", jwtExpirationMs);
		config.put("sessionTimeoutMinutes", sessionTimeoutMs / 60000);
		config.put("jwtExpirationMinutes", jwtExpirationMs / 60000);
		return config;
	}
}
