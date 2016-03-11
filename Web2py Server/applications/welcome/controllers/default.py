# -*- coding: utf-8 -*-
# this file is released under public domain and you can use without limitations

#########################################################################
## This is a sample controller
## - index is the default action of any application
## - user is required for authentication and authorization
## - download is for downloading files uploaded in the db (does streaming)
#########################################################################
import base64

KEY = 'GAmaW6d90bvBKDOw60b68e9t10356Dqy'

def index():
   if auth.user_id == None:
      redirect(URL('default', 'user', args=['login']))
   else:
      rows = db().select(db.userData.ALL)
      d = []
      for r in rows:
         d.append({'user': r.email})
      addForm = SQLFORM(db.userData)
      if addForm.process().accepted:
         session.flash = T('Added new user')
         redirect(URL('default', 'index'))
      return dict(userData=d, addForm=addForm)
   """
   example action using the internationalization operator T and flash
   rendered by views/default/index.html or views/generic.html

   if you need a simple wiki simply replace the two lines below with:
   return auth.wiki()
   """
   return response.json({'response': 'ok'})

#this gets all messages for a particular computer
#must be passed: computerName, key
def get_messages():
   try:
      if request.vars.computerName == None or request.vars.key == None or request.vars.key != KEY:
         theDict = dict(messageInfo=[])
         theDict.update({'response':'error'})
         return response.json(theDict)  
      comp = request.vars.computerName
      rows = db(db.messageData.computerName == comp).select()
      d = []
      for r in rows:
         d.append({'computerName': r.computerName,
                           'computerNumber':r.computerNumber,
                           'messageData':r.messageData,
                           'problem': r.problem,
                           'timeCreated': r.timeCreated})
      theDict = dict(messageInfo=d)
      theDict.update({'response':'ok'})
      return response.json(theDict)
   except:
      theDict = dict(messageInfo=[])
      theDict.update({'response':'error'})
      return response.json(theDict)

#this allows user to post a message to the database and updates the status for that particular computer
#must be passed: computerName, computerNumber, messageData, problem, key
def post_message():
   try:
      if request.vars.computerName == None or request.vars.computerNumber == None or request.vars.messageData == None or request.vars.problem == None or request.vars.key == None or request.vars.key != KEY:
         return response.json({'response':'error'})
      db.messageData.insert(computerName = request.vars.computerName,
               computerNumber = request.vars.computerNumber,
               messageData = request.vars.messageData,
               problem = request.vars.problem)
      status = ''
      if request.vars.problem == 'fixed':
         status = 'working'
      else:
         status = 'broken'
      db.computerStatus.update_or_insert((db.computerStatus.computerName == request.vars.computerName),
               computerName = request.vars.computerName,
               currentStatus=status)
      return response.json({'response': 'ok'})
   except:
      return response.json({'response':'error'})

#this gets the most recent status of a computer
#must be passed: computerName, key      
def get_status():
   try:
      if request.vars.computerName == None or request.vars.key == None or request.vars.key != KEY:
         theDict = dict(problemInfo={})
         theDict.update({'response':'error'})
         return response.json(theDict)
      comp = request.vars.computerName
      rows = db(db.computerStatus.computerName == comp).select()
      d = {}
      for r in rows:
         d = {'computerName': r.computerName,
                           'currentStatus': r.currentStatus}
      theDict = dict(problemInfo=d)
      theDict.update({'response': 'ok'})
      return response.json(theDict)
   except:
      theDict = dict(problemInfo={})
      theDict.update({'response':'error'})
      return response.json(theDict)
      
#this is used to add a user to the database, assuming they don't already exist in it
#must be passed: email, password, key      
def add_user():
   try:
      if request.vars.email == None or request.vars.password == None or request.vars.key == None or request.vars.key != KEY:
         return response.json({'response':'error'})
      rows = db(db.userData.email == request.vars.email).select()
      if len(rows) == 0:
         return response.json({'response': 'user not authorized'})
      db.userData.update_or_insert((db.userData.email == request.vars.email),
                                       email = request.vars.email,
                                       password = base64.b64encode(request.vars.password))
      return response.json({'response':'ok'})
   except:
      return response.json({'response':'error'})

#this is used to check if a user exists in the system
#must be passed: email, password, key     
def get_user():
   try:
      if request.vars.email == None or request.vars.password == None or request.vars.key == None or request.vars.key != KEY:
         return response.json({'response':'error'})
      rows = db(db.userData.email == request.vars.email).select()
      login = {}
      if len(rows) == 0:
         login.update({'login':'failed'})
      for r in rows:
         if r.password != base64.b64encode(request.vars.password):
            login.update({'login':'failed'})
         else:
            login.update({'login': 'success'})
      theDict = dict(userInfo=login)
      theDict.update({'response':'ok'})
      return response.json(theDict)
   except:
      theDict = dict(userInfo={})
      theDict.update({'response':'error'})
      return response.json(theDict)
      
#this is used to get the schedule for a particular computer
#NOTE: THIS DATA WILL NEED TO BE PARSED IN-APP FOR ENTRIES WITHIN A CERTAIN DATE (THIS WILL RETURN EVERYTHING)
#must be passed: computerName, key
def get_schedule():
   try:
      if request.vars.computerName == None or request.vars.key == None or request.vars.key != KEY:
         theDict = dict(scheduleInfo=[])
         theDict.update({'response':'error'})
         return response.json(theDict)  
      rows = db(db.schedule.computerName == request.vars.computerName).select()
      d = []
      for r in rows:
         d.append({'computerName': r.computerName,
                        'beginTime': r.beginTime,
                        'endTime': r.endTime,
                        'dateReserved': r.dateReserved,
                        'email': r.email})
      theDict = dict(scheduleInfo=d)
      theDict.update({'response':'ok'})
      return response.json(theDict)
   except:
      theDict = dict(scheduleInfo=[])
      theDict.update({'response':'error'})
      return response.json(theDict)

#this allows a user to schedule a computer on a certain date for a particular block of time
#must be passed: computerName, beginTime, endTime, dateReserved, email, key
def schedule_computer():
   try:
      if request.vars.computerName == None or request.vars.beginTime == None or request.vars.endTime == None or request.vars.dateReserved == None or request.vars.email == None or request.vars.key == None or request.vars.key != KEY:
         return response.json({'response': 'ok'})
      db.schedule.insert(computerName = request.vars.computerName,
               beginTime = request.vars.beginTime,
               endTime = request.vars.endTime,
               dateReserved = request.vars.dateReserved,
               email = request.vars.email)
      return response.json({'response':'ok'})
   except:
      return response.json({'response':'error'})
      
def delete_user():
   db(db.userData.email == request.args(0)).delete()
   redirect(URL('default', 'index', args=[]))
   return
      
#----------------------------------FOR DEVELOPMENT PURPOSES-----------------------------------------------
def wipe_server():
   try:
      if request.vars.server == 'messageData':
         db(db.messageData).delete()
      if request.vars.server == 'statusData':
         db(db.computerStatus).delete()
      if request.vars.server == 'userData':
         db(db.userData).delete()
      if request.vars.server == 'scheduleData':
         db(db.schedule).delete()
      if request.vars.server == 'all':
         db(db.messageData).delete()
         db(db.computerStatus).delete()
         db(db.userData).delete()
         db(db.schedule).delete()
      return response.json({'response': 'ok'})
   except:
      return response.json({'response': 'error'})

def initialize_default_data():
   try:
      db.userData.insert(email = 'jbernay@ucsc.edu', password = base64.b64encode('temp'))
      db.userData.insert(email = 'aparvis@ucsc.edu', password = base64.b64encode('temp'))
      db.userData.insert(email = 'srsmall@ucsc.edu', password = base64.b64encode('temp'))
      db.userData.insert(email = 'lccunnin@ucsc.edu', password = base64.b64encode('temp'))      
      db.messageData.insert(computerName = 'SNOW', computerNumber = '364', messageData = 'Initialization of server data.', problem = 'fixed')
      db.messageData.insert(computerName = 'LIGHTNING', computerNumber = '364', messageData = 'Initialization of server data.', problem = 'fixed')
      db.messageData.insert(computerName = 'SEPHIROTH', computerNumber = '368', messageData = 'Initialization of server data.', problem = 'fixed')
      db.messageData.insert(computerName = 'ZELDA', computerNumber = '368', messageData = 'Initialization of server data.', problem = 'fixed')
      db.messageData.insert(computerName = 'SEAMUS', computerNumber = '368', messageData = 'Initialization of server data.', problem = 'fixed')
      db.messageData.insert(computerName = 'LINK', computerNumber = '368', messageData = 'Initialization of server data.', problem = 'fixed')
      db.messageData.insert(computerName = 'CMDRKEEN', computerNumber = '368', messageData = 'Initialization of server data.', problem = 'fixed')
      db.messageData.insert(computerName = 'VICVIPER', computerNumber = '368', messageData = 'Initialization of server data.', problem = 'fixed')
      db.messageData.insert(computerName = 'MARIO', computerNumber = '368', messageData = 'Initialization of server data.', problem = 'fixed')
      db.messageData.insert(computerName = 'BRAID', computerNumber = '368', messageData = 'Initialization of server data.', problem = 'fixed')
      db.messageData.insert(computerName = 'DONKEYKONG', computerNumber = '368', messageData = 'Initialization of server data.', problem = 'fixed')
      db.messageData.insert(computerName = 'MASSEFFECT', computerNumber = '366', messageData = 'Initialization of server data.', problem = 'fixed')
      db.messageData.insert(computerName = 'HALFLIFE', computerNumber = '366', messageData = 'Initialization of server data.', problem = 'fixed')
      db.messageData.insert(computerName = 'CALLOFDUTY', computerNumber = '366', messageData = 'Initialization of server data.', problem = 'fixed')
      db.messageData.insert(computerName = 'LEFT4DEAD', computerNumber = '366', messageData = 'Initialization of server data.', problem = 'fixed')      
      db.computerStatus.insert(computerName = 'SNOW', currentStatus = 'working')
      db.computerStatus.insert(computerName = 'LIGHTNING', currentStatus = 'working')
      db.computerStatus.insert(computerName = 'SEPHIROTH', currentStatus = 'working')
      db.computerStatus.insert(computerName = 'ZELDA', currentStatus = 'working')
      db.computerStatus.insert(computerName = 'SEAMUS', currentStatus = 'working')
      db.computerStatus.insert(computerName = 'LINK', currentStatus = 'working')
      db.computerStatus.insert(computerName = 'CMDRKEEN', currentStatus = 'working')
      db.computerStatus.insert(computerName = 'VICVIPER', currentStatus = 'working')
      db.computerStatus.insert(computerName = 'MARIO', currentStatus = 'working')
      db.computerStatus.insert(computerName = 'BRAID', currentStatus = 'working')
      db.computerStatus.insert(computerName = 'DONKEYKONG', currentStatus = 'working')
      db.computerStatus.insert(computerName = 'MASSEFFECT', currentStatus = 'working')
      db.computerStatus.insert(computerName = 'HALFLIFE', currentStatus = 'working')
      db.computerStatus.insert(computerName = 'CALLOFDUTY', currentStatus = 'working')
      db.computerStatus.insert(computerName = 'LEFT4DEAD', currentStatus = 'working')
      db.auth_user.update_or_insert(email = 'admin@ucsc.edu', password = 'pbkdf2(1000,20,sha512)$a3c3b38df3459c84$218a620950f4b33654a59205a4caee0f10112af5')
      return response.json({'response':'ok'})
   except:
      return response.json({'response':'error'})
#-------------------------------------------------------------------------------------------------

def user():
    """
    exposes:
    http://..../[app]/default/user/login
    http://..../[app]/default/user/logout
    http://..../[app]/default/user/register
    http://..../[app]/default/user/profile
    http://..../[app]/default/user/retrieve_password
    http://..../[app]/default/user/change_password
    http://..../[app]/default/user/manage_users (requires membership in
    http://..../[app]/default/user/bulk_register
    use @auth.requires_login()
        @auth.requires_membership('group name')
        @auth.requires_permission('read','table name',record_id)
    to decorate functions that need access control
    """
    if request.args(0) != 'login':
      redirect(URL('default', 'user', args=['login']))
    if auth.user_id != None:
      redirect(URL('default', 'index', args=[]))
    return dict(form=auth())


@cache.action()
def download():
    """
    allows downloading of uploaded files
    http://..../[app]/default/download/[filename]
    """
    return response.download(request, db)


def call():
    """
    exposes services. for example:
    http://..../[app]/default/call/jsonrpc
    decorate with @services.jsonrpc the functions to expose
    supports xml, json, xmlrpc, jsonrpc, amfrpc, rss, csv
    """
    return service()


