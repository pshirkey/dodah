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

class Base(webapp.RequestHandler):
    
    template_path = '/templates/

    def get(self):
        self.post()
    def post(self):
        pass
    def generate (self, fileIn, values):
        ROOT_PATH = os.path.dirname(__file__)
        path = os.path.join(ROOT_PATH + self.template_path, str(fileIn))
        self.response.out.write(template.render(path, values))
        
    @property
    def enviroment(self):
        if not hasattr(self, "_enviroment"):
            self._enviroment = None
            self._enviroment = models.Environment.get_for_current_environment(server=server)
        return self._enviroment;        
     
    def unique_result(self, array):
        unique_results = []
        for obj in array:
            if obj not in unique_results:
                unique_results.append(obj)
            return unique_results
