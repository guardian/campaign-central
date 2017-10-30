 Cloudformation templates
 ========================
 
 ### campaign-central dynamo Db
 
 ```
 aws cloudformation update-stack \
    --stack-name CampaignCentralDynamo-DEV \
    --template-body file:///$PWD/dynamoDb.yaml \
    --parameters  ParameterKey=Stage,ParameterValue=DEV \
    --profile composer \
    --region eu-west-1
```
 
 ### campaign-central-backups
 
 This template is an AWS Data Pipeline which schedules a weekly job that drops the contents of an old dev-environment
  dynamodb table, then copies the prod table into the empty dev table. This allows dev to stay roughly in sync with prod.
 
 #### Managing multiple dynamodb tables
 There are several dynamodb tables in Campaign Central:
 - `campaign-central-PROD-campaigns`
 - `campaign-central-PROD-referrals`
 - `campaign-central-PROD-campaign-notes`
 - `campaign-central-PROD-campaign-content`
 - `campaign-central-PROD-campaign-page-views`
 - `campaign-central-PROD-campaign-uniques`
 - `campaign-central-PROD-campaign-analytics-latest`
 
 It's straightforward to reuse the cloudformation template to create
 a source-destination backup pair, eg. 
 
 For a backup from `campaign-central-PROD-campaigns` to `campaign-central-DEV-campaigns`, use the aws cli command:

```
 aws cloudformation create-stack \
    --stack-name campaign-central-campaign-backups \
    --template-body file:///$PWD/campaign-central-backups.yaml \
    --parameters  ParameterKey=SourceTableName,ParameterValue=campaign-central-PROD-campaigns ParameterKey=DestinationTableName,ParameterValue=campaign-central-DEV-campaigns \
    --profile composer \
    --region eu-west-1
```