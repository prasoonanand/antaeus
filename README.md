## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will schedule payment of those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## Instructions

Fork this repo with your solution. Ideally, we'd like to see your progression through commits, and don't forget to update the README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

## Developing

Requirements:
- \>= Java 11 environment

Open the project using your favorite text editor. If you are using IntelliJ, you can open the `build.gradle.kts` file and it is gonna setup the project in the IDE for you.

### Building

```
./gradlew build
```

### Running

There are 2 options for running Anteus. You either need libsqlite3 or docker. Docker is easier but requires some docker knowledge. We do recommend docker though.

*Running Natively*

Native java with sqlite (requires libsqlite3):

If you use homebrew on MacOS `brew install sqlite`.

```
./gradlew run
```

*Running through docker*

Install docker for your platform

```
docker build -t antaeus
docker run antaeus
```

### App Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── buildSrc
|  | gradle build scripts and project wide dependency declarations
|  └ src/main/kotlin/utils.kt 
|      Dependencies
|
├── pleo-antaeus-app
|       main() & initialization
|
├── pleo-antaeus-core
|       This is probably where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|       Module interfacing with the database. Contains the database 
|       models, mappings and access layer.
|
├── pleo-antaeus-models
|       Definition of the Internal and API models used throughout the
|       application.
|
└── pleo-antaeus-rest
        Entry point for HTTP REST API. This is where the routes are defined.
```

### Main Libraries and dependencies
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine

Happy hacking 😁!


### Starting on Project
1. 7-Apr Understanding project structure, code and running docker command, found that command on readme `docker build -t antaeus` has a typo.
         It should be `docker build . -t antaeus` or just ask to run docker-start.sh...
         
     **Worked for 30 min.**
        
2. 8-Apr Deciding on putting a boundary on the problem (Problem statement/ Functional requirements) :
    i. Raise Invoice for customers every month. 
      * Nothing to do if Invoice already paid for that month.
      * Send the invoice again if in PENDING state for that month.
      * Generate Invoice if not for that month.
      * Need to consider what if a customer starts in middle of the month. 
        Billing will generate an invoice for the remaining day to 1st of month.
      
       As defined in the challenge, we will only schedule payments of invoices already generated.
       
    ii. Next is PLAN, what plan a customer is having based on which the invoice amount should be generated.
      * Like Simple, Corporate etc... No need for consideration.
       
    iii. Another is Amount change based on Customer country as it's a Data point.
      * The invoice amount will change, now this can be done in 2 different ways.
        a. Having a common value for all customer country and charging/ chaning amount based on FX Rates.
        b. Having/ Defining different PLAN based on countries.
        
       No need for consideration.
    
    iv. Scheduling(Time) of invoice generation based on Regions.
      * This is required as let's suppose we run our job in UTC timezone at 12 MID night(start of month 1st). 
      Then for few countries it's still 30th or 31st. which are in UTC-.
      * We need to define Regions based on which the customers are raised an invoice.
     
    v. Deciding on, can a customer have multiple invoices. (Out of Scope)
    
    vi. Admin charges/ interest/ suspension of account if invoice failed from scheduling side and had to be billed from admin.(Out of Scope)
    
    Vii. Customer Mailing/Intimation service - considering it to be a third party and should be handled via queues. not considering as part of this challenge. (out of scope)
         
    viii. Notification service (Out of scope)
            
    ix. Accounting Service (out of scope)
           
    x. Reporting Service (Out of scope)
    
    xi. Alerting Service (Out of scope)
    
     **Worked for 1 hour.**
          
3. 9-Apr Solution based on above assumptions.
        
        We will Generate Invoice in advance with pending state and raise payments on the due date. (Already being done, so not getting into this.)     
        Epic: 
            As a user, I want to be billed every start of the month, if not already paid just once for that month.
        Stories: 
            0. As pleo, Billing of customers should be done every start of the month, if not already paid just once for that month. (DONE)
            1. As pleo, Billing should be able to send multiple payments calls async based on paymentsProvider bandwidth for different invoices.
            2. As pleo, If the monthly job does not runs raise a P1. (out of scope)
            3. As Pleo, I should be able to retry for failed payments for 5 times after 10 mins interval. These reties should only happen for valid failed reasons.
            4. As pleo, I should be able to raise an alert as a P3. If all retries have not been able to pass the payments for valid failed reasons.(out of scope)
            5. As pleo, I should be able to reaise an alert as a P2. If the failed reason from payments is `CurrencyMismatchException` or `CustomerNotFoundException`.(out of scope)
            6. As pleo, I should be able to raise manual billings, if for some user the payment failed and manual intervention is required. (DONE)
            7. As pleo, I should be able to charge customers from the start of subscription to start of next month. (Out of scope)
            8. As pleo, I should be able to change invoice state manually with reasons.(Out of scope)
            9. As pleo, I should be able to run the PENDING payment job manually as well. (DONE)
        
        Not considing the points which were out of scope in problem statement.
        
        
        Core Tech requirements.
            Horizontal scaling.
            Durability.
            External config.
            Extensible.
            Basically with 12 factors.
        
            Coding Standards at least with SOLID principles.
        
     **Worked for 1 hour 30 min.**
        
4. 10-Apr Coding
        Starting with the 1st ticket 0, this is big so sub divided it into 4 parts DB, Service, scheduler and API.
        Starting from the DB side 1st.
        On db Side both fetch and update has been batched, and the batch size has been externalized in a config (so that 
        it can be changed to proper batch-size).
        Also, exposed an API for pending invoices to help the admin and dev ops team.
     
        Getting stuck at a lot of small places as everything is new and have not worked.
        
     **Worked for 2 hours.**        
        
5. 12-Apr Coding
      
        Starting on Service, scheduler and API.
        Have used quartz cron task which will run at 12 Noon every 1st of the month to minimize time zone side effects.
        Created a `BillingScheduler` in core, but it would be best to be as a separate service or may be lambda.
        Exposed a custom API `PATCH /rest/v1/billing/pending` for running the billing manually if for some reason the 
        cron fails, or the business wants to have a custom time at which the billing should be done.
        Added new status for invoices:
        `CURRENCY_MISMATCH` -> If there is a currency mismatch
        `CUSTOMER_NOT_FOUNT` -> If customer not found
        `CUSTOMER_REJECTED` -> If payment gets rejected by customer bank
        `FAILURE` -> If there is a network issue, or some other exception (will be re-schedule again)
        `ERROR` -> Retry failed.
        
        only `FAILURE` will be retried/ rescheduled, and all else should be check.
        This has been done so that we can easily identify problems for dev ops/ business purposes as well.
        
        Ticket 0 and 9 is complete.
        Added test cases.
        
        Changed the test case on ScheduleingService as it was flaky.
        
        Done with ticket 6. Not handling java.lang.IllegalArgumentException: No enum constant io.pleo.antaeus.models.InvoiceStatus
        in `/rest/v1/invoices/update/status/:id` call.

      **Worked for 3 hours.**

6. 13-Apr Coding
            Code refactor - changed API to `/rest/v1/invoices/status/:{status}/{:page}` -> to get invoices based on status
                                            `PATCH /rest/v1/billing/{:status}` -> to bill invoices based on status
            Also the scheduling task will now run based on status type.
            SchedulingServiceTest is really flaky gave 30 mins for this and leaving it for now.
            
            Starting on 3
            Have changed the method billingPendingInvoice to billing invoice and to identify if it's the last run for retry.
            It looks ugly but will try to refactor later if have time.
            
      **Worked for 3 hours.**


**Tech Assumptions:**

Scenario if multiple cron job run concurrently has not been handled if we take that then we have to take a lock on DB end or
make the page variable thread safe being global to the services.
