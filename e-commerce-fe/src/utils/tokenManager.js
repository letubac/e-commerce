// Token expiration manager
class TokenManager {
  constructor() {
    this.warningShown = false;
    this.checkInterval = null;
  }

  // Parse JWT to get expiration time
  parseJwt(token) {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (e) {
      return null;
    }
  }

  // Check if token is expired
  isTokenExpired(token) {
    if (!token) return true;
    
    const decoded = this.parseJwt(token);
    if (!decoded || !decoded.exp) return true;
    
    const expirationTime = decoded.exp * 1000; // Convert to milliseconds
    const currentTime = Date.now();
    
    const isExpired = currentTime >= expirationTime;
    
    // Debug log
    console.log('Token expiration check:', {
      currentTime: new Date(currentTime).toLocaleString(),
      expirationTime: new Date(expirationTime).toLocaleString(),
      timeUntilExpiration: Math.floor((expirationTime - currentTime) / 1000 / 60) + ' minutes',
      isExpired
    });
    
    return isExpired;
  }

  // Get time until expiration in milliseconds
  getTimeUntilExpiration(token) {
    if (!token) return 0;
    
    const decoded = this.parseJwt(token);
    if (!decoded || !decoded.exp) return 0;
    
    const expirationTime = decoded.exp * 1000;
    const currentTime = Date.now();
    
    return Math.max(0, expirationTime - currentTime);
  }

  // Check if token will expire soon (within 5 minutes)
  willExpireSoon(token) {
    const timeUntilExpiration = this.getTimeUntilExpiration(token);
    return timeUntilExpiration > 0 && timeUntilExpiration <= 5 * 60 * 1000; // 5 minutes
  }

  // Start monitoring token expiration
  startMonitoring(onExpired, onExpiringSoon) {
    // Clear existing interval
    if (this.checkInterval) {
      clearInterval(this.checkInterval);
    }

    // Reset warning state for new session
    this.warningShown = false;

    // Check every 30 seconds
    this.checkInterval = setInterval(() => {
      const token = localStorage.getItem('token');
      
      if (!token) {
        clearInterval(this.checkInterval);
        return;
      }

      if (this.isTokenExpired(token)) {
        clearInterval(this.checkInterval);
        if (onExpired) onExpired();
      } else if (this.willExpireSoon(token) && !this.warningShown) {
        this.warningShown = true;
        if (onExpiringSoon) onExpiringSoon();
      }
    }, 30000); // Check every 30 seconds

    // Do NOT do an immediate synchronous check here — it causes race conditions
    // right after login where startMonitoring is called just after localStorage.setItem.
    // The 30-second interval above handles expiry detection safely.
  }

  // Stop monitoring
  stopMonitoring() {
    if (this.checkInterval) {
      clearInterval(this.checkInterval);
      this.checkInterval = null;
    }
    this.warningShown = false;
  }

  // Reset warning flag
  resetWarning() {
    this.warningShown = false;
  }
}

export default new TokenManager();
