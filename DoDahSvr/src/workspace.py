import cgi
import logging
import os.path
import time
import urllib
import simplejson
import datetime
from data import models
from google.appengine.ext import db
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext.db import djangoforms
from django.utils import simplejson as json
from google.appengine.api import mail 
from django.core.validators import email_re
import lilcookies
import hashlib
import django.newforms as forms
import random

class Base(webapp.RequestHandler):
    
    template_path = '/templates/'
    login_template_name = "login.html"

    def get(self):
        self.post()
    def post(self):
        if self.current_user:
            self.do_post()
        else:
            self.generate( Login.template, None )
            
    def do_post(self):
        pass    
    
    def param(self, name):
        return self.request.get(name)
    
    def write(self, mes):
        self.response.out.write(mes)
        
    def _create_item_code(self):
        alphabet = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
        return "".join(random.sample(alphabet,8))
    
    def generate (self, fileIn, values):
        if values is None:
            values = {}
        
        if 'current_user' not in values.keys():
            values['current_user'] = self.current_user
        
        ROOT_PATH = os.path.dirname(__file__)
        path = os.path.join(ROOT_PATH + self.template_path, str(fileIn))
        self.response.out.write(template.render(path, values))
        
    @property
    def enviroment(self):
        if not hasattr(self, "_enviroment"):
            self._enviroment = None
            self._enviroment = models.Environment.get_for_current_environment()
        return self._enviroment;  
    
    @property
    def current_user(self):
        if not hasattr(self, "_current_user"):
            self._current_user = None
            #host = os.environ.get("SERVER_NAME")
            #if host == "localhost":
            #    self._current_user = models.User.get_test_user()
            #    return self._current_user      
            
            cookieutil = lilcookies.LilCookies(self, self.enviroment.cookie_secret )
            uid = cookieutil.get_secure_cookie(name='uid')
            if uid:
                self._current_user = db.get(uid)
        return self._current_user
     
class Login(Base):
    
    url = "/login"
    template = "login.html"
    
    def post(self):        
        cookieutil = lilcookies.LilCookies(self, self.enviroment.cookie_secret )
        cookieutil.clear_all_cookies()
        email = self.param('email')
        password = self.param('password')    
        if email and password:    
            passw = hashlib.md5(password).hexdigest()
            user = models.User.get_by_key_name(email)
            if user and user.password == passw:
                cookieutil = lilcookies.LilCookies(self, self.enviroment.cookie_secret )
                cookieutil.set_secure_cookie(name = 'uid', value=str(user.key()), expires_days=365)
                self.redirect(Index.url)
            else:
                self.generate( Login.template, { 'message':"unable to log in" } )
        else:
                self.generate( Login.template, None )
                
class Logout(Base):
    
    url = "/logout"
    
    def do_post(self):
        cookieutil = lilcookies.LilCookies(self, self.enviroment.cookie_secret )
        cookieutil.clear_all_cookies()
        self.redirect(Login.url)
            
class Locations(Base):
    
    url = "/locations"
    template = "locations.html"
    
    def do_post(self):
        
        self.generate(Locations.template, { 'locations':models.Location.all() })

class LocationForm(djangoforms.ModelForm):
    class Meta:
        model = models.Location
        exclude = ( 'rating', 'geoboxes', 'items', 'updated', 'created', 'meta', '_class', 'create_ts', 'modify_ts', 'edited_by',)
        category = forms.ModelChoiceField(queryset=models.Category.all())
        description = forms.CharField(widget=forms.Textarea(attrs={'cols': 130, 'rows': 20}))
        
class EditLocation(Base):
    
    url = "/editlocation"
    template = "editlocation.html"
    
    @classmethod
    def get_url(cls, itemId):
        return EditLocation.url + "?id=" + str(itemId)
    
    def do_post(self):
        id = self.request.get('id')
        
        locForm = None
        loc = None
       
        if id and id != "":
            loc = db.get(id)
            locForm = LocationForm(instance=loc)
        else:
            locForm = LocationForm()
                
        template_values = {
                           'loc':loc,
                           'form':locForm,
                           }
        self.generate(EditLocation.template, template_values)
        
class SaveLocation(Base):
    url = "/savelocation"
    
    def do_post(self):
        id = self.param('id')
        entity = None
        if id and id != "":
            locForm = LocationForm(data=self.request.POST, instance=db.get(id))
        else:
            locForm = LocationForm(data=self.request.POST)        
        if locForm.is_valid():
            entity = locForm.save(commit=True)
            entity.owner = self.current_user
            entity.save() 
            id = entity.key()   
        
        template_values = {
                               'loc':entity,
                               'form':locForm
                               }
        self.generate(EditLocation.template, template_values)
        
class SaveItem(Base):
    url = "/saveitem"
    
    def do_post(self):
        id = self.param('id')
        locid = self.param('locid')
        name = self.param('name')
        details = self.param('details')
        difficulty = self.param('difficulty')
        
        item = db.get(id)
        if item:
            item.name = name
            item.details = details
            item.diffculty = float(difficulty)
            item.save()
        self.redirect(EditLocation.get_url(locid))
        
class GenerateItems(Base):
    
    url="/generate"
    
    def do_post(self):
        id = self.param('id')
        number = self.param('number')
        num = 5
        loc = db.get(id)
        if loc:
            if number:
                num = int(number)
            for i in range(1, int(num)):
                code = self._create_item_code()
                item = models.Item.get_by_key_name(code)
                while item:
                    code = self._create_item_code()
                    item = models.Item.get_by_key_name(code)
                item = models.Item(key_name=code, location=loc, code=code)
                item.put()
        self.redirect(EditLocation.get_url(id))

class Index(Base):
    
    url = "/"    
    template_name = "usermenu.html"
    
    def do_post(self):
        self.generate(Index.template_name, None)
        
class LoadTestData(Base):
    
    url = "/loadtestdata"
    
    def post(self):
        
        models.User.get_test_user()
        self.redirect(Index.url)
        
        cat = models.Category(name="Food/Beverage", description="")
        cat.put()
        
        subcat = models.Category(name="Resturant", description="", parent_category=cat)
        subcat.put()
        
        subcat = models.Category(name="Coffee House", description="", parent_category=cat)
        subcat.put()
        
        red = models.Location(owner=self.current_user, location=db.GeoPt(37, -122))
        red.address = "310 SW 3rd Street"
        red.name = "Red Horse"
        red.description = "Red Horse Coffee"
        red.category = subcat
        red.city = "Corvallis"
        red.state = "OR"
        red.zip = "97333"
        red.phone_number = "(541) 757-3025"
        red.rating = 99
        red.put()
        
        
        
        
        


application = webapp.WSGIApplication([
                                       (Index.url, Index),
                                       (Login.url, Login),
                                       (Logout.url,Logout),
                                       (LoadTestData.url, LoadTestData), 
                                       (Locations.url, Locations),
                                       (EditLocation.url, EditLocation),
                                       (SaveLocation.url, SaveLocation),
                                       (SaveItem.url,SaveItem),
                                       (GenerateItems.url, GenerateItems)                                  
                                     ], debug=True)



def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()

