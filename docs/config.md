
# Configuration

* `delay` (int) - Delay (milliseconds) between rule starts 
* `rules` (String) - Location of the rule(s) (e.g. /tmp/rules or /tmp/myRule.groovy) 
* `sources` (Seq) - Sources 
	* `name` (Option) - Should uniquely specified in case of multiple sources 
	* `url` (String) - Connection url (e.g. elasticSearch:http://localhost:9200) 
	* `params` (Option) -  
* `targetSettings` (TargetSettings) - Target specific settings (shared with all rules) 
	* `hipchat` (HipchatSettings) - HipChat settings 
		* `accessToken` (String) - Access token https://developer.atlassian.com/hipchat/guide/hipchat-rest-api 
		* `baseUrl` (String) - "https://api.hipchat.com/v2/" or your company server 
	* `mailServer` (MailServerSettings) - Mail Server (SMTP) settings 
		* `host` (String) - e.g. "smtp.gmail.com" 
		* `port` (String) - e.g. "465" 
		* `auth` (boolean) - e.g. true 
		* `username` (String) - e.g. "myemail@test.com" 
		* `password` (String) - your password 
	* `slack` (SlackSettings) - Slack settings 
		* `accessToken` (String) - Token 
	* `jira` (JiraSettings) - Jira settings 
		* `url` (String) - e.g. "http://jira.company.com" 
		* `username` (String) - Jira username 
		* `password` (String) - Jira password 
* `home` (String) - Location of the L3rt's home. Used for the state storing 

    