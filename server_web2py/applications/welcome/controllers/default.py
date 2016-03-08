# -*- coding: utf-8 -*-
# this file is released under public domain and you can use without limitations

#########################################################################
## This is a sample controller
## - index is the default action of any application
## - user is required for authentication and authorization
## - download is for downloading files uploaded in the db (does streaming)
#########################################################################
import base64

def index():
    """
    example action using the internationalization operator T and flash
    rendered by views/default/index.html or views/generic.html

    if you need a simple wiki simply replace the two lines below with:
    return auth.wiki()
    """
    return response.json({'response': 'ok'})

#this gets all messages for a particular computer
#must be passed: computerName
def get_messages():
   try:
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
#must be passed: computerName, computerNumber, messageData, problem
def post_message():
   try:
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
#must be passed: computerName      
def get_status():
   try:
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
#must be passed: email, password      
def add_user():
   try:
      rows = db(db.userData.email == request.vars.email).select()
      if len(rows) != 0:
         return response.json({'response': 'user exists'})
      db.userData.insert(email = request.vars.email,
               password = base64.b64encode(request.vars.password))
      return response.json({'response':'ok'})
   except:
      return response.json({'response':'error'})

#this is used to check if a user exists in the system
#must be passed: email, password      
def get_user():
   try:
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
#must be passed: computerName
def get_schedule():
   try:
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
#must be passed: computerName, beginTime, endTime, dateReserved, email
def schedule_computer():
   try:
      db.schedule.insert(computerName = request.vars.computerName,
               beginTime = request.vars.beginTime,
               endTime = request.vars.endTime,
               dateReserved = request.vars.dateReserved,
               email = request.vars.email)
      return response.json({'response':'ok'})
   except:
      return response.json({'response':'error'})
      
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
      return response.json({'response': 'ok'})
   except:
      return response.json({'response': 'error'})

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


