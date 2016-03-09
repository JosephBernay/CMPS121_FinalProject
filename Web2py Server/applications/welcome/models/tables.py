from datetime import datetime

# database to hold reports that users have posted. 
db.define_table('messageData',
                  Field('computerName'),
                  Field('computerNumber'),
                  Field('messageData'),
                  Field('problem'),       # what the issue was flagged as
                  Field('timeCreated', 'datetime', default=datetime.utcnow())) 

# database to hold the most recent status of each computer.
db.define_table('computerStatus',
                  Field('computerName'),
                  Field('currentStatus'))

# database to hold the email and password of each user (password will be encrypted)
db.define_table('userData',
                  Field('email'),
                  Field('password'))
                  
# database to hold the times in which each computer is reserved, and the email of who reserved them.
db.define_table('schedule',
                  Field('computerName'),
                  Field('beginTime'),
                  Field('endTime'),
                  Field('dateReserved'),
                  Field('email'))
                  