# 🔐 KD-AuthPlugin

> BungeeCord authentication plugin with premium/cracked account support

[![BungeeCord](https://img.shields.io/badge/BungeeCord-Latest-orange.svg)](https://www.spigotmc.org/wiki/bungeecord/)
[![Java](https://img.shields.io/badge/Java-8-blue.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-Required-lightblue.svg)](https://www.mysql.com/)

## 📖 Overview

KD-AuthPlugin is a BungeeCord authentication system that supports both premium (paid Minecraft accounts) and cracked (offline mode) players on the same network. It uses an external API to verify premium status and routes players accordingly.

## ✨ Features

### 🎯 Core Functionality

- **Premium Account Detection** - Automatic verification via HTTP API
- **Dual Authentication System**:
  - Premium players → auto-login with Mojang authentication
  - Cracked players → custom login/register system
- **MySQL Storage** - Persistent user data
- **Join Throttling** - Anti-spam protection
- **Premium Status Caching** - 1-hour cache to reduce API calls
- **Dedicated Auth Server** - Separate server for non-premium authentication

### 🔄 Player Flow

```
Player connects
    ↓
Premium check via HTTP API
    ↓
┌─────────────────┬─────────────────┐
│   Premium       │   Cracked       │
│   (true)        │   (false)       │
├─────────────────┼─────────────────┤
│ Online mode ON  │ Online mode OFF │
│ Auto-login      │ Send to 'auth'  │
│ → lobby server  │ /register or    │
│                 │ /login required │
│                 │ → lobby server  │
└─────────────────┴─────────────────┘
```

## 🚀 Setup

### Requirements

- BungeeCord proxy server
- MySQL database
- Premium verification API endpoint
- Two backend servers:
  - `auth` - Authentication server (for cracked players)
  - `lobby` - Main lobby server (post-authentication)

### Installation

1. **Download** the plugin JAR
2. **Place** in `plugins/` folder of your BungeeCord proxy
3. **Configure** `config.yml` (see below)
4. **Restart** BungeeCord
5. **Set up** backend servers in BungeeCord config:

```yaml
# config.yml (BungeeCord main config)
servers:
  auth:
    address: localhost:25566
    motd: 'Authentication Server'
    restricted: false
  lobby:
    address: localhost:25567
    motd: 'Main Lobby'
    restricted: false
```

### Configuration

Create/edit `plugins/Auth/config.yml`:

```yaml
mysql:
  host: "localhost"
  port: 3306
  user: "root"
  password: "password"
  name: "minecraft"
  prefix: "auth_"

messages:
  # Premium player messages
  login_withPremium: "&aWitaj ponownie! Zalogowano automatycznie."
  
  # Cracked player messages
  login_withNoPremium: "&eProsze sie zalogowac."
  login_rightUsage: "&eUzyj: /login <haslo>"
  register_rightUsage: "&eUzyj: /register <haslo> <powtorz>"
  
  # Error messages
  html_error: "&cBlad polaczenia z API. Sprobuj ponownie."
  html_exception: "&cWystapil blad. Skontaktuj sie z administracja."
  connected_wrongPresent: "&cBlad autoryzacji. Sprobuj ponownie."
  login_tooMuchTime: "&cPrzekroczono czas logowania."
```

### Premium API Setup

The plugin requires an HTTP API endpoint to verify premium accounts:

**API Endpoint Format:**
```
http://yourapi.com/haspaid/?name=PlayerName&auth=YourAPIKey
```

**Expected Response:**
- `true` - Player has premium account
- `false` - Player is cracked
- `error` - API error (player will be kicked)

**Update in code:** `LoginManager.java` line 38:
```java
final String html = getHTML("http://yourapi.com/haspaid/?name=" + name + "&auth=apikey");
```

## 💻 Commands

### Player Commands

| Command | Description | Usage |
|---------|-------------|-------|
| `/register <password> <password>` | Register new account | `/register mypass123 mypass123` |
| `/login <password>` | Login to account | `/login mypass123` |

### Admin Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/adminauth reload` | `auth.admin` | Reload configuration |
| `/adminauth info <player>` | `auth.admin` | View player auth info |

## 🗃️ Database Schema

```sql
CREATE TABLE `auth_users` (
    `ID` INT(11) NOT NULL AUTO_INCREMENT,
    `uuid` VARCHAR(36) NOT NULL,
    `firstIP` VARCHAR(100) NOT NULL,
    `lastIP` VARCHAR(100) NOT NULL,
    `lastName` VARCHAR(16) NOT NULL,
    `password` VARCHAR(48) NOT NULL,
    `premium` TINYINT(1) NOT NULL,
    PRIMARY KEY (`ID`)
);
```

**Fields:**
- `uuid` - Player unique ID
- `firstIP` - First login IP address
- `lastIP` - Last login IP address
- `lastName` - Current player name
- `password` - Hashed password (or "PREMIUM" for premium accounts)
- `premium` - Premium status (1 = premium, 0 = cracked)

## 🔧 Technical Details

### Premium Detection Flow

```java
// 1. Player connects (PreLoginEvent)
LoginManager.registerEvent(playerName, event)
    ↓
// 2. Check cache
Boolean cached = premium.getIfPresent(name.toLowerCase())
if (cached != null) {
    return cached; // Use cached result
}
    ↓
// 3. HTTP API request
String response = getHTML("http://api/haspaid/?name=" + name)
    ↓
// 4. Parse response
boolean isPremium = Boolean.parseBoolean(response)
    ↓
// 5. Cache result (1 hour)
premium.put(name.toLowerCase(), isPremium)
    ↓
// 6. Set online mode
event.getConnection().setOnlineMode(isPremium)
```

### Authentication Flow

**Premium Player:**
```
Connect → Premium check (API) → true → Online mode ON
    ↓
ServerConnectedEvent → Create/Update user → Connect to 'lobby'
```

**Cracked Player:**
```
Connect → Premium check (API) → false → Offline mode
    ↓
ServerConnectedEvent → Connect to 'auth' server
    ↓
Player uses /register or /login
    ↓
Verification successful → Connect to 'lobby'
```

### Join Throttling

The plugin includes protection against connection spam:

```java
// LoginManager
private int currentJoints;
private final int MAX_JOINS_PER_INTERVAL = 10; // Example

// Task runs every 2 seconds
scheduler.schedule(() -> loginManager.setCurrentJoints(0), 2L, 2L, TimeUnit.SECONDS);
```

### Caching System

Premium status is cached using Guava Cache:

```java
Cache<String, Boolean> premium = CacheBuilder.newBuilder()
    .expireAfterWrite(1L, TimeUnit.HOURS)
    .build();
```

**Benefits:**
- Reduced API calls
- Faster player connections
- Less load on premium verification API

## 📂 Project Structure

```
pl.krayday.auth/
│
├── Auth.java                      # Main plugin class
│
├── data/
│   └── User.java                  # User data model
│
├── manager/
│   ├── UserManager.java           # User CRUD operations
│   └── LoginManager.java          # Premium check & auth logic
│
├── commands/
│   ├── RegisterCommand.java       # /register
│   ├── LoginCommand.java          # /login
│   └── AdminAuthCommand.java      # /adminauth
│
├── listeners/
│   ├── PreLoginListener.java      # Join throttling
│   ├── ProxyPingListener.java     # Server list ping
│   └── ServerConnectedListener.java # Post-login routing
│
├── store/
│   ├── Store.java                 # Database interface
│   ├── StoreMySQL.java            # MySQL implementation
│   └── Entry.java                 # Data model interface
│
└── utils/
    ├── Util.java                  # General utilities
    ├── TimeUtil.java              # Time formatting
    └── Logger.java                # Logging utilities
```

## 🔐 Security Considerations

### Current Implementation

⚠️ **Password Storage:** Plain text passwords in database
⚠️ **SQL Injection:** String concatenation in queries
⚠️ **API Security:** API key hardcoded in source

### Recommended Improvements

```java
// 1. Hash passwords with BCrypt
import org.mindrot.jbcrypt.BCrypt;

String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
boolean matches = BCrypt.checkpw(password, hashed);

// 2. Use PreparedStatements
PreparedStatement ps = conn.prepareStatement(
    "INSERT INTO users (uuid, password) VALUES (?, ?)"
);
ps.setString(1, uuid.toString());
ps.setString(2, hashedPassword);

// 3. Move API credentials to config
config.getString("api.endpoint")
config.getString("api.key")
```

## 🛠️ Troubleshooting

### Players stuck on auth server

**Cause:** Login/register not working
**Fix:** Check console for errors, verify database connection

### Premium players not auto-logging

**Cause:** API not responding or returning wrong data
**Fix:** Check API endpoint, verify response format

### "Too many requests" error

**Cause:** Join throttling triggered
**Fix:** Increase throttling limits or disable

### Players kicked with "html_error"

**Cause:** Premium API timeout or error
**Fix:** Verify API is online, check network connectivity

## 📝 Example Configuration

```yaml
mysql:
  host: "localhost"
  port: 3306
  user: "minecraft"
  password: "securepassword"
  name: "auth_db"
  prefix: "auth_"

messages:
  login_withPremium: "&a&lWELCOME &7back!"
  login_withNoPremium: "&e&lAUTH &7Please login or register."
  login_rightUsage: "&e/login <password>"
  register_rightUsage: "&e/register <password> <confirm>"
  html_error: "&cAPI error. Please try again."
  html_exception: "&cSystem error. Contact admin."
  connected_wrongPresent: "&cAuthentication failed."
  login_tooMuchTime: "&cLogin timeout exceeded."
```

## 🤝 Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 📄 License

This project is open source and available under the MIT License.

## 👨‍💻 Author

**KrayDay Development**
- Package: `pl.krayday.auth`

---

**Note:** This plugin requires additional configuration for production use, including proper password hashing and SQL injection prevention.
