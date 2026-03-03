# PhishNet Demo Test Script

This document provides a step-by-step guide for demonstrating the PhishNet Cybersecurity Incident Reporting System, including Argon2 password hashing functionality.

## Prerequisites

Before running the demo, ensure you have:
- ✅ Java 17 or higher installed
- ✅ MySQL Server running and accessible
- ✅ Maven installed and configured
- ✅ Database created (run `PhishNet-structure.sql` first)
- ✅ Initial data inserted (run `PhishNet-inserts.sql`)

---

## Demo Setup

### Step 1: Database Setup

1. **Create the database structure:**
   ```bash
   mysql -u root -p < PhishNet-structure.sql
   ```

2. **Insert initial data:**
   ```bash
   mysql -u root -p < PhishNet-inserts.sql
   ```

3. **Verify database connection:**
   - Check `src/main/resources/application.properties` (or template)
   - Ensure MySQL credentials are correct

### Step 2: Build and Run Application

1. **Compile the project:**
   ```bash
   mvn clean compile
   ```

2. **Run the application:**
   ```bash
   mvn javafx:run
   ```
   OR use the batch file:
   ```bash
   run.bat
   ```

---

## Demo Scenarios

### Scenario 1: User Registration (Argon2 Password Hashing)

**Objective:** Demonstrate secure password hashing during user registration.

**Steps:**
1. Launch the application
2. Click **"Sign Up"** button on the login screen
3. Fill in the registration form:
   - **Name:** `John Doe`
   - **Email:** `john.doe@example.com`
   - **Password:** `SecurePass123!`
4. Click **"Register"**
5. **Expected Result:** 
   - Success message displayed
   - Password is hashed using Argon2id before storage
   - User can now log in with these credentials

**What to Highlight:**
- Password is never stored in plaintext
- Argon2id hash is generated with secure parameters
- Hash includes salt, parameters, and version info

**Verification (Optional - Database Check):**
```sql
SELECT VictimID, Name, ContactEmail, PasswordHash, AccountStatus 
FROM Victims 
WHERE ContactEmail = 'john.doe@example.com';
```
- Note: PasswordHash should start with `$argon2id$`
- PasswordHash should be ~100+ characters long
- Original password is NOT visible

---

### Scenario 2: User Login (Password Verification)

**Objective:** Demonstrate password verification using Argon2.

**Steps:**
1. On the login screen, enter:
   - **Email:** `john.doe@example.com`
   - **Password:** `SecurePass123!`
2. Click **"Login"**
3. **Expected Result:**
   - Successful login
   - Redirected to Victim Dashboard
   - Password verification uses Argon2id

**Test Invalid Password:**
1. Try logging in with wrong password: `WrongPass123`
2. **Expected Result:** Error message, login fails

**What to Highlight:**
- Password verification happens securely
- Even if database is compromised, passwords cannot be reversed
- Verification uses the same Argon2 parameters as hashing

---

### Scenario 3: Admin Login

**Objective:** Demonstrate admin authentication with Argon2.

**Steps:**
1. On the login screen, click **"Admin Login"** link
2. Enter admin credentials:
   - **Email:** `admin@phishnet.com`
   - **Password:** `PhishNetAdmin124`
3. Click **"Login"**
4. **Expected Result:**
   - Successful admin login
   - Redirected to Admin Dashboard
   - Access to administrative features

**What to Highlight:**
- Admin passwords also use Argon2id
- Same security standards for all user types
- Admin accounts are pre-configured with Argon2 hashes

---

### Scenario 4: Report an Incident

**Objective:** Demonstrate core functionality after authentication.

**Steps:**
1. Log in as a victim user (from Scenario 1 or 2)
2. Navigate to **"Report Incident"** or similar option
3. Fill in incident details:
   - **Attack Type:** Select from dropdown (e.g., "Phishing")
   - **Description:** Enter incident details
   - **Perpetrator Info:** Enter attacker details if available
   - **Evidence:** Upload file (optional)
4. Submit the report
5. **Expected Result:**
   - Report successfully created
   - Report ID generated
   - Report visible in "My Reports"

**What to Highlight:**
- Secure session management
- Data integrity maintained
- User can track their reports

---

### Scenario 5: Admin Review and Management

**Objective:** Demonstrate admin capabilities.

**Steps:**
1. Log in as admin (from Scenario 3)
2. Navigate to **"Review Reports"** or dashboard
3. View pending incident reports
4. Review a report and:
   - Update threat level
   - Add evaluation notes
   - Change victim status if needed
5. **Expected Result:**
   - Admin can manage all reports
   - Changes are logged
   - System tracks administrative actions

**What to Highlight:**
- Role-based access control
- Admin-only features protected
- Audit trail maintained

---

## Technical Verification Tests

### Test 1: Argon2 Hash Format Verification

**Purpose:** Verify that passwords are stored in correct Argon2 format.

**Steps:**
1. Register a new user with password: `TestPass123`
2. Query database:
   ```sql
   SELECT PasswordHash FROM Victims 
   WHERE ContactEmail = 'test@example.com';
   ```
3. **Expected Format:**
   ```
   $argon2id$v=19$m=65536,t=3,p=4$[salt]$[hash]
   ```
4. Verify components:
   - Starts with `$argon2id$`
   - Contains version `v=19`
   - Contains memory cost `m=65536`
   - Contains time cost `t=3`
   - Contains parallelism `p=4`
   - Contains salt and hash segments

---

### Test 2: Password Hash Uniqueness

**Purpose:** Verify that same password produces different hashes (due to salt).

**Steps:**
1. Register User 1 with password: `SamePassword123`
2. Register User 2 with password: `SamePassword123`
3. Query database:
   ```sql
   SELECT ContactEmail, PasswordHash FROM Victims 
   WHERE PasswordHash LIKE '%SamePassword123%' 
   OR ContactEmail IN ('user1@test.com', 'user2@test.com');
   ```
4. **Expected Result:**
   - Both users can log in with `SamePassword123`
   - But their PasswordHash values are DIFFERENT
   - This proves salt is working correctly

---

### Test 3: Password Verification Consistency

**Purpose:** Verify that password verification works correctly.

**Steps:**
1. Create a test user with known password
2. Attempt login with:
   - ✅ Correct password → Should succeed
   - ❌ Wrong password → Should fail
   - ❌ Empty password → Should fail
   - ❌ Password with extra spaces → Should fail
3. **Expected Result:**
   - Only exact correct password works
   - All variations fail appropriately

---

### Test 4: Legacy Hash Support (If Applicable)

**Purpose:** Verify backward compatibility with old SHA-256 hashes.

**Note:** This test is only relevant if migrating from SHA-256.

**Steps:**
1. Manually insert a user with SHA-256 hash:
   ```sql
   INSERT INTO Victims (Name, ContactEmail, PasswordHash) 
   VALUES ('Legacy User', 'legacy@test.com', '[SHA-256_HASH]');
   ```
2. Attempt login with correct password
3. **Expected Result:**
   - Login succeeds (if legacy support is enabled)
   - System can verify both Argon2 and SHA-256 hashes

---

## Performance Demonstration

### Argon2 Hashing Speed

**Purpose:** Show that Argon2 is secure but still performant.

**Steps:**
1. Register multiple users in sequence
2. Observe:
   - Each registration takes ~100-500ms (typical)
   - Hash generation is fast enough for user experience
   - Security is maintained without significant delay

**What to Highlight:**
- Argon2 parameters are tuned for balance
- 3 iterations, 64 MiB memory, 4 threads
- Fast enough for UX, secure enough for production

---

## Security Features to Highlight

During the demo, emphasize:

1. **Password Hashing:**
   - Argon2id (winner of Password Hashing Competition)
   - Resistant to GPU attacks
   - Resistant to side-channel attacks
   - Includes salt for uniqueness

2. **Password Storage:**
   - Never stored in plaintext
   - Self-contained hash format
   - Parameters embedded in hash

3. **Password Verification:**
   - Constant-time comparison (prevents timing attacks)
   - Uses same Argon2 function instance
   - Supports legacy hashes during migration

4. **Input Validation:**
   - Password length limits (DoS protection)
   - Email validation
   - SQL injection prevention

---

## Troubleshooting

### Issue: Application won't start
- **Check:** Database connection in `application.properties`
- **Check:** MySQL server is running
- **Check:** Java version is 17+

### Issue: Login fails even with correct password
- **Check:** Password hash format in database
- **Check:** Argon2 parameters match SecurityUtils
- **Check:** No extra whitespace in stored hash

### Issue: Registration fails
- **Check:** Email is unique
- **Check:** Password meets requirements
- **Check:** Database connection is active

---

## Demo Checklist

Before presenting, verify:

- [ ] Database is set up and running
- [ ] Application compiles without errors
- [ ] At least one admin account exists
- [ ] Test user accounts can be created
- [ ] Login functionality works
- [ ] Password hashes are in correct format
- [ ] Admin dashboard is accessible
- [ ] Incident reporting works
- [ ] Evidence upload works (if applicable)

---

## Quick Demo Script (5 minutes)

**For a quick demo, follow this condensed flow:**

1. **Start Application** (30 sec)
   - Show login screen
   - Explain Argon2 security

2. **Register New User** (1 min)
   - Create account: `demo@test.com` / `DemoPass123`
   - Show registration success
   - Explain password hashing

3. **Login as User** (30 sec)
   - Login with new credentials
   - Show dashboard

4. **Report Incident** (1 min)
   - Create sample incident report
   - Show report submission

5. **Admin Login** (1 min)
   - Login as admin: `admin@phishnet.com` / `PhishNetAdmin124`
   - Show admin dashboard
   - Review submitted report

6. **Database Verification** (1 min)
   - Show password hash in database
   - Explain Argon2 format
   - Emphasize security

**Total Time:** ~5 minutes

---

## Notes for Presenters

- **Emphasize Security:** Always mention that passwords are never stored in plaintext
- **Show Hash Format:** Display an actual Argon2 hash from the database
- **Explain Parameters:** Briefly mention memory cost, iterations, parallelism
- **Compare to Old Methods:** Mention why Argon2 is better than SHA-256/MD5
- **Real-World Context:** Explain that this follows industry best practices

---

## Additional Test Accounts

**Pre-configured Admin Accounts:**
- `admin@phishnet.com` / `PhishNetAdmin124`
- `zach_benedict_hallare@dlsu.edu.ph` / `PhishNetAdmin124`
- `benette_campo@dlsu.edu.ph` / `PhishNetAdmin124`
- `brent_rebollos@dlsu.edu.ph` / `PhishNetAdmin124`
- `georgina_ravelo@dlsu.edu.ph` / `PhishNetAdmin124`

All admin accounts use the same password: `PhishNetAdmin124`

---

**End of Demo Script**


