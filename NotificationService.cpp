#include <iostream>
#include <string>
#include <vector>
#include <unordered_map>
#include <memory>
#include <algorithm>

using namespace std;

// Client 
//   ↓
// NotificationManager (Orchestrator + Validation)
//   ↓
// PermissionValidator (Client permission checks)
//   ↓
// NotificationChannelRegistry (Service registry)
//   ↓
// NotificationService implementations (Email, SMS, Push)

// ==================== ENUMS ====================

enum class Channel {
    EMAIL,
    SMS,
    PUSH,
    SLACK,
    WHATSAPP
};

enum class ClientType {
    FREE,
    PREMIUM,
    ENTERPRISE
};

enum class NotificationStatus {
    SUCCESS,
    FAILED,
    CHANNEL_NOT_FOUND,
    PERMISSION_DENIED
};

// Helper function to convert enum to string for printing
string channelToString(Channel channel) {
    switch(channel) {
        case Channel::EMAIL: return "EMAIL";
        case Channel::SMS: return "SMS";
        case Channel::PUSH: return "PUSH";
        case Channel::SLACK: return "SLACK";
        case Channel::WHATSAPP: return "WHATSAPP";
        default: return "UNKNOWN";
    }
}

// ==================== MODELS ====================

struct User {
    string userId;
    string name;
    string email;
    string phone;
    string deviceToken;
    
    User(string id, string n, string e, string p, string token = "")
        : userId(id), name(n), email(e), phone(p), deviceToken(token) {}
};

struct Message {
    string subject;
    string body;
    int priority; // 1-5, 5 being highest
    
    Message(string subj, string b, int prio = 3)
        : subject(subj), body(b), priority(prio) {}
};

struct NotificationResult {
    Channel channel;
    NotificationStatus status;
    string errorMessage;
    
    NotificationResult(Channel ch, NotificationStatus st, string err = "")
        : channel(ch), status(st), errorMessage(err) {}
};

// ==================== NOTIFICATION SERVICE BASE ====================

class NotificationService {
protected:
    Channel channelType;
    
public:
    NotificationService(Channel type) : channelType(type) {}
    virtual ~NotificationService() = default;
    
    Channel getChannelType() const { return channelType; }
    
    // Pure virtual method - each channel implements its own logic
    virtual NotificationStatus send(const User& user, const Message& msg) = 0;
    
    // Prevent copying
    NotificationService(const NotificationService&) = delete;
    NotificationService& operator=(const NotificationService&) = delete;
};

// ==================== CONCRETE IMPLEMENTATIONS ====================

class EmailNotificationService : public NotificationService {
private:
    static EmailNotificationService* instance;
    
    EmailNotificationService() : NotificationService(Channel::EMAIL) {}
    
public:
    static EmailNotificationService* getInstance() {
        if (!instance) {
            instance = new EmailNotificationService();
        }
        return instance;
    }
    
    NotificationStatus send(const User& user, const Message& msg) override {
        cout << "[EMAIL] Sending to: " << user.email << endl;
        cout << "  Subject: " << msg.subject << endl;
        cout << "  Body: " << msg.body << endl;
        
        // Simulate email sending logic
        if (user.email.empty()) {
            cout << "  Status: FAILED - No email address" << endl;
            return NotificationStatus::FAILED;
        }
        
        cout << "  Status: SUCCESS" << endl;
        return NotificationStatus::SUCCESS;
    }
};

class SMSNotificationService : public NotificationService {
private:
    static SMSNotificationService* instance;
    
    SMSNotificationService() : NotificationService(Channel::SMS) {}
    
public:
    static SMSNotificationService* getInstance() {
        if (!instance) {
            instance = new SMSNotificationService();
        }
        return instance;
    }
    
    NotificationStatus send(const User& user, const Message& msg) override {
        cout << "[SMS] Sending to: " << user.phone << endl;
        cout << "  Message: " << msg.body << endl;
        
        if (user.phone.empty()) {
            cout << "  Status: FAILED - No phone number" << endl;
            return NotificationStatus::FAILED;
        }
        
        cout << "  Status: SUCCESS" << endl;
        return NotificationStatus::SUCCESS;
    }
};

class PushNotificationService : public NotificationService {
private:
    static PushNotificationService* instance;
    
    PushNotificationService() : NotificationService(Channel::PUSH) {}
    
public:
    static PushNotificationService* getInstance() {
        if (!instance) {
            instance = new PushNotificationService();
        }
        return instance;
    }
    
    NotificationStatus send(const User& user, const Message& msg) override {
        cout << "[PUSH] Sending to device: " << user.deviceToken << endl;
        cout << "  Title: " << msg.subject << endl;
        cout << "  Body: " << msg.body << endl;
        
        if (user.deviceToken.empty()) {
            cout << "  Status: FAILED - No device token" << endl;
            return NotificationStatus::FAILED;
        }
        
        cout << "  Status: SUCCESS" << endl;
        return NotificationStatus::SUCCESS;
    }
};

class SlackNotificationService : public NotificationService {
private:
    static SlackNotificationService* instance;
    
    SlackNotificationService() : NotificationService(Channel::SLACK) {}
    
public:
    static SlackNotificationService* getInstance() {
        if (!instance) {
            instance = new SlackNotificationService();
        }
        return instance;
    }
    
    NotificationStatus send(const User& user, const Message& msg) override {
        cout << "[SLACK] Sending to: @" << user.name << endl;
        cout << "  Message: " << msg.body << endl;
        cout << "  Status: SUCCESS" << endl;
        return NotificationStatus::SUCCESS;
    }
};

// Initialize static members
EmailNotificationService* EmailNotificationService::instance = nullptr;
SMSNotificationService* SMSNotificationService::instance = nullptr;
PushNotificationService* PushNotificationService::instance = nullptr;
SlackNotificationService* SlackNotificationService::instance = nullptr;

// ==================== CHANNEL REGISTRY ====================

class NotificationChannelRegistry {
private:
    static NotificationChannelRegistry* instance;
    unordered_map<Channel, NotificationService*> channels;
    
    NotificationChannelRegistry() {}
    
public:
    static NotificationChannelRegistry* getInstance() {
        if (!instance) {
            instance = new NotificationChannelRegistry();
        }
        return instance;
    }
    
    void registerChannel(NotificationService* service) {
        if (service) {
            channels[service->getChannelType()] = service;
            cout << "[REGISTRY] Registered channel: " 
                 << channelToString(service->getChannelType()) << endl;
        }
    }
    
    void unregisterChannel(Channel channelType) {
        channels.erase(channelType);
        cout << "[REGISTRY] Unregistered channel: " 
             << channelToString(channelType) << endl;
    }
    
    NotificationService* getChannel(Channel channelType) {
        auto it = channels.find(channelType);
        return (it != channels.end()) ? it->second : nullptr;
    }
    
    bool isChannelAvailable(Channel channelType) {
        return channels.find(channelType) != channels.end();
    }
    
    // Prevent copying
    NotificationChannelRegistry(const NotificationChannelRegistry&) = delete;
    NotificationChannelRegistry& operator=(const NotificationChannelRegistry&) = delete;
};

NotificationChannelRegistry* NotificationChannelRegistry::instance = nullptr;

// ==================== PERMISSION VALIDATOR ====================

class PermissionValidator {
private:
    unordered_map<ClientType, vector<Channel>> clientPermissions;
    
    PermissionValidator() {
        // Define permissions for each client type
        clientPermissions[ClientType::FREE] = {Channel::EMAIL};
        
        clientPermissions[ClientType::PREMIUM] = {
            Channel::EMAIL, 
            Channel::SMS, 
            Channel::PUSH
        };
        
        clientPermissions[ClientType::ENTERPRISE] = {
            Channel::EMAIL, 
            Channel::SMS, 
            Channel::PUSH,
            Channel::SLACK,
            Channel::WHATSAPP
        };
    }
    
    static PermissionValidator* instance;
    
public:
    static PermissionValidator* getInstance() {
        if (!instance) {
            instance = new PermissionValidator();
        }
        return instance;
    }
    
    vector<Channel> getAllowedChannels(ClientType clientType) {
        return clientPermissions[clientType];
    }
    
    bool isChannelAllowed(ClientType clientType, Channel channel) {
        const auto& allowed = clientPermissions[clientType];
        return find(allowed.begin(), allowed.end(), channel) != allowed.end();
    }
    
    vector<Channel> filterAllowedChannels(ClientType clientType, 
                                         const vector<Channel>& requestedChannels) {
        vector<Channel> filtered;
        const auto& allowed = clientPermissions[clientType];
        
        for (const auto& channel : requestedChannels) {
            if (find(allowed.begin(), allowed.end(), channel) != allowed.end()) {
                filtered.push_back(channel);
            }
        }
        
        return filtered;
    }
};

PermissionValidator* PermissionValidator::instance = nullptr;

// ==================== NOTIFICATION MANAGER ====================

class NotificationManager {
private:
    NotificationChannelRegistry* registry;
    PermissionValidator* validator;
    
    NotificationManager() {
        registry = NotificationChannelRegistry::getInstance();
        validator = PermissionValidator::getInstance();
    }
    
    static NotificationManager* instance;
    
public:
    static NotificationManager* getInstance() {
        if (!instance) {
            instance = new NotificationManager();
        }
        return instance;
    }
    
    vector<NotificationResult> sendNotification(
        ClientType clientType,
        const User& user,
        const Message& message,
        const vector<Channel>& requestedChannels
    ) {
        vector<NotificationResult> results;
        
        cout << "\n========== NOTIFICATION REQUEST ==========" << endl;
        cout << "Client Type: " << static_cast<int>(clientType) << endl;
        cout << "User: " << user.name << endl;
        cout << "Message: " << message.subject << endl;
        
        // Step 1: Validate permissions
        vector<Channel> allowedChannels = 
            validator->filterAllowedChannels(clientType, requestedChannels);
        
        // Check for permission denials
        for (const auto& channel : requestedChannels) {
            if (find(allowedChannels.begin(), allowedChannels.end(), channel) 
                == allowedChannels.end()) {
                cout << "\n[PERMISSION DENIED] Channel: " 
                     << channelToString(channel) << endl;
                results.emplace_back(channel, NotificationStatus::PERMISSION_DENIED,
                                   "Client does not have permission for this channel");
            }
        }
        
        // Step 2: Send to allowed channels
        for (const auto& channel : allowedChannels) {
            cout << "\n----- Processing Channel: " 
                 << channelToString(channel) << " -----" << endl;
            
            // Check if channel is registered
            if (!registry->isChannelAvailable(channel)) {
                cout << "[ERROR] Channel not available in registry" << endl;
                results.emplace_back(channel, NotificationStatus::CHANNEL_NOT_FOUND,
                                   "Channel service not registered");
                continue;
            }
            
            // Get service and send notification
            NotificationService* service = registry->getChannel(channel);
            NotificationStatus status = service->send(user, message);
            
            results.emplace_back(channel, status);
        }
        
        cout << "\n=========================================" << endl;
        
        return results;
    }
    
    // Prevent copying
    NotificationManager(const NotificationManager&) = delete;
    NotificationManager& operator=(const NotificationManager&) = delete;
};

NotificationManager* NotificationManager::instance = nullptr;

// ==================== BOOTSTRAP / INITIALIZATION ====================

class NotificationBootstrap {
public:
    static void initialize() {
        cout << "\n===== INITIALIZING NOTIFICATION SYSTEM =====" << endl;
        
        auto* registry = NotificationChannelRegistry::getInstance();
        
        // Register all available channels
        registry->registerChannel(EmailNotificationService::getInstance());
        registry->registerChannel(SMSNotificationService::getInstance());
        registry->registerChannel(PushNotificationService::getInstance());
        registry->registerChannel(SlackNotificationService::getInstance());
        
        cout << "============================================\n" << endl;
    }
};

// ==================== MAIN / DEMO ====================

int main() {
    // Initialize the notification system
    NotificationBootstrap::initialize();
    
    // Get notification manager
    NotificationManager* manager = NotificationManager::getInstance();
    
    // Create test users
    User freeUser("1", "Alice", "alice@example.com", "+1234567890", "device123");
    User premiumUser("2", "Bob", "bob@example.com", "+0987654321", "device456");
    User enterpriseUser("3", "Charlie", "charlie@example.com", "+1122334455", "device789");
    
    // Create test message
    Message msg("System Alert", "Your account has been updated successfully!", 4);
    
    // Test Case 1: FREE user tries to use EMAIL (allowed) and SMS (denied)
    cout << "\n\n========== TEST CASE 1: FREE USER ==========" << endl;
    vector<Channel> freeUserChannels = {Channel::EMAIL, Channel::SMS};
    manager->sendNotification(ClientType::FREE, freeUser, msg, freeUserChannels);
    
    // Test Case 2: PREMIUM user uses EMAIL, SMS, PUSH (all allowed)
    cout << "\n\n========== TEST CASE 2: PREMIUM USER ==========" << endl;
    vector<Channel> premiumUserChannels = {Channel::EMAIL, Channel::SMS, Channel::PUSH};
    manager->sendNotification(ClientType::PREMIUM, premiumUser, msg, premiumUserChannels);
    
    // Test Case 3: ENTERPRISE user uses all channels
    cout << "\n\n========== TEST CASE 3: ENTERPRISE USER ==========" << endl;
    vector<Channel> enterpriseUserChannels = {
        Channel::EMAIL, Channel::SMS, Channel::PUSH, Channel::SLACK
    };
    manager->sendNotification(ClientType::ENTERPRISE, enterpriseUser, msg, 
                            enterpriseUserChannels);
    
    // Test Case 4: PREMIUM user tries to use SLACK (denied)
    cout << "\n\n========== TEST CASE 4: PERMISSION DENIAL ==========" << endl;
    vector<Channel> deniedChannels = {Channel::SLACK, Channel::WHATSAPP};
    manager->sendNotification(ClientType::PREMIUM, premiumUser, msg, deniedChannels);
    
    return 0;
}