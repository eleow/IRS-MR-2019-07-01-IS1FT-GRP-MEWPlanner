from django.http import HttpResponse, HttpResponseRedirect
from django.views.generic import TemplateView
from requests.exceptions import HTTPError
from django.template import loader

from django.contrib.auth import login, authenticate
from django.contrib.auth.forms import UserCreationForm
from django.shortcuts import render, redirect
from django.contrib.auth import get_user_model
User = get_user_model()

import requests
from json import dumps, loads
from .forms import NewUserForm, UserUpdateForm

import datetime
from dateutil.relativedelta import relativedelta

import subprocess
from os.path import abspath, dirname, join, realpath, isfile
from os import getcwd, chdir, chmod
from os import stat as stat2
import stat
import random

import csv
import json
import threading

ACTIVITY_DICT = {
    0: 1.2,
    1: 1.375,
    2: 1.465,
    3: 1.55,
    4: 1.725,
    5: 1.9
}


def index(request):
  return HttpResponse("Nothing to see here. You're at the app index.")

def signup(request):
    if (request.user.is_authenticated):
        return redirect('/home/')

    if request.method == 'POST':
        form = NewUserForm(request.POST)
        if form.is_valid():
            form.save()
            username = form.cleaned_data.get('username')
            raw_password = form.cleaned_data.get('password1')
            user = authenticate(username=username, password=raw_password)
            login(request, user)
            return redirect('/home/')
    else:
        form = NewUserForm()
    return render(request, 'signup.html', {'form': form})

def createNewPlan(pathJar, configFileName, pathOutfile):
    print(pathJar)
    outfile = open(pathOutfile,'w+')
    # # Note - popen is non-blocking, call is blocking.
    # p = subprocess.call(['java', '-jar', pathJar, "config.ini"], stdout=outfile, stderr=outfile, shell=True)
    p = subprocess.Popen(['java', '-jar', pathJar, configFileName], stdout=outfile, stderr=outfile, shell=False) #shell must be False for unix (See https://stackoverflow.com/questions/2400878/why-subprocess-popen-doesnt-work-when-args-is-sequence)
    outfile.close()

def CalculateCalories(gender, wt, ht, age, activity):
    if (gender == 0):
        bmr = 10*wt + 6.25*ht - 5*age + 5
    else: 
        bmr = 10*wt + 6.25*ht - 5*age - 161 
    return activity * bmr 

def CreateConfigIni(dirPath, fileName, debug_mode, sp_calories, dev_calories, max_sodium, numdays=7, cuisine='0', takes_beef=1):

   CUISINE_CHOICES = {'0': 'none', '1': 'chinese', '2': 'malay', '3': 'indian', '4': 'western'}
   cuisine_str = CUISINE_CHOICES.get(cuisine, 'none')

   f = open(join(dirPath, fileName),'w+') 
   f.write("[settings]\n")
   f.write("debug_mode = " + str(debug_mode) + "\n\n")
   f.write("[targets]\n")
   f.write("sp_calories = " + str(sp_calories) + "\n")
   f.write("dev_calories = " + str(dev_calories) + "\n")
   f.write("max_sodium = " + str(max_sodium) + "\n")
   f.write("days = " + str(numdays) + "\n")
   f.write("max_history = " + str(numdays) + "\n")
   f.write("max_sugar = 30\n")
   f.write("max_caffeine = 1\n")
   f.write("carbs_frac = 0.5\n")
   f.write("dev_carbs = 0.05\n")
   f.write("fats_frac = 0.3\n")
   f.write("diabetic = 1\n")
   f.write("prefers = " + cuisine_str+ "\n")
   f.write("takes_beef = " + str(takes_beef) + "\n")

   f.close()



class ViewPlanView(TemplateView):

    def parse(self, f):
        # TODO
        with f as csvfile:
            readCSV = csv.reader(csvfile, delimiter=",")
            foodPlans = {}
            isMetaData = True
            isEndOfFile = False
            targets = []
            for row in readCSV:
                # print(row)
                if ("calories" in row[0]): targets = ''.join(i for i in row[0] if i.isdigit() or i == ".")
                if (row[0] == "1"): isMetaData = False
                if ('~END' in row[0]): isEndOfFile = True
                if (not isMetaData and not isEndOfFile):
                    day = row[0]
                    meal = row[1]

                    details = {
                        "type": row[2],
                        "name": row[3],
                        "calories": row[4],
                        "carbohydrates": row[5],
                        "fats": row[6],
                        "protein": row[7],
                        "sodium": row[8],
                        "serving": row[9],
                        "sugar": row[10] if len(row) >= 11 else 0    # to do
                    }
                    
                    if not day in foodPlans: foodPlans[day] = {}
                    if not meal in foodPlans[day]: foodPlans[day][meal] = []
                    # foodPlans[day][meal].append({"type": typeF, "name": nameF, "cal": cal})
                    foodPlans[day][meal].append(details)
        # print(targets)
        # print(foodPlans)
        return foodPlans, targets, isEndOfFile
        # return json.dumps(foodPlans, indent=2)

    def get(self, request, **kwargs):
        if (request.user.is_authenticated):

            dir_path = dirname(realpath(__file__))
            dir_path = abspath(join(dir_path, "../optaPlanner/"))
            chdir(dir_path)
            
            fileId = ''.join(e for e in request.user.username if e.isalnum()) # strip any special characters from username 

            if (isfile(fileId + "_results.txt")):
                f = open(fileId + "_results.txt",'r')
            else: 
                # Redirect to createPlan
                return redirect('/createPlan/' ) 
            
            parsed, targets, isEndOfFile = self.parse(f)

            if (isEndOfFile):
                details = request.GET.get('details')
                if (details=="more"):
                    return render(request, 'viewPlanDetailed.html', context={"result": parsed, "targets": targets})
                else:
                    return render(request, 'viewPlan.html', context={"result": parsed, "targets": targets})
            else:
                myText = ""
                myTextArr = ["Initiating magic solver", 
                    "Gathering magical ingredients", "Warming up magical pot", "Cutting garnishes",
                    "Finding cooks", "Finding helpers", "Preheating oven", "Mixing ingredients", "Slicing ingredients"]
                if (parsed != None and len(parsed) == 0): 
                    myText = random.choice(myTextArr)

                return render(request, 'waitingPage.html', context={"done": len(parsed), "text": myText})
        else:
           return redirect('/login/')

class CreatePlanView(TemplateView):
    def get(self, request, **kwargs):

        if (request.user.is_authenticated):
            u = request.user
            age_rdelta = relativedelta(datetime.date.today(), u.birth_date)
            age = age_rdelta.years + age_rdelta.months/12 + age_rdelta.days / 365.25
            wt = u.weight.__float__()   # weight in kg
            ht = u.height.__float__()   # height in cm
            act = ACTIVITY_DICT.get(u.activity, 1.2)  # activity factor
            
            # Calculate derived fields
            bmi =  wt / (ht/100)**2
            amr = CalculateCalories(u.gender, wt, ht, age, act)

            return render(request, 'createPlan.html', context={
                "BMI": bmi,
                "Calories" : amr,
                "AGE": age
            })
        else:
           return redirect('/login/')
    def post(self, request, **kwargs):
        if (request.user.is_authenticated):

            # Update form values to user profile if changed
            form = UserUpdateForm(request.POST, instance=request.user)
            if form.is_valid:
                r = form.save(commit=False)
                r.user = request.user
                r.save()

            dir_path = dirname(realpath(__file__))
            dir_path = abspath(join(dir_path, "../optaPlanner/"))
            chdir(dir_path)

            # Generate input file for optaplanner            
            fileId = ''.join(e for e in request.user.username if e.isalnum()) # strip any special characters from username
            target_calories = request.POST["calories"]
            cuisine_preference = request.POST["cuisine_preference"]
            takes_beef = request.POST["takes_beef"]

            configFileName = fileId + "_config.ini"
            pathJar = abspath(join(dir_path, "optaplanner.jar"))
            pathOutfile = join(dir_path, fileId + "_results.txt")
            CreateConfigIni(dir_path, configFileName, 0, target_calories, 0.05, 2300, 7, cuisine = cuisine_preference, takes_beef = takes_beef)

            # for heroku. In unix, need to do chmod +x
            st = stat2('./optaplanner.jar')
            chmod("./optaplanner.jar", st.st_mode | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH)

            # break thread for processing
            threading.Thread(target=createNewPlan, args=(pathJar, configFileName, pathOutfile)).start()
            return HttpResponseRedirect('/viewPlan/')
        else:
           return redirect('/login/')


class HomePageView(TemplateView):
    def get(self, request, **kwargs):
        if (request.user.is_authenticated):
            return render(request, 'home.html', context=None)
        else:
           return redirect('/login/')

class ComingSoonPageView(TemplateView):
    def get(self, request, **kwargs):
        return render(request, 'comingSoon.html', context=None)

class DebugPageView(TemplateView):
    def get(self, request, **kwargs):
        # import pdb; pdb.set_trace()
        return render(request, 'debug.html', context=None)

class TestQueryView(TemplateView):
    def get(self, request, **kwargs):
        query_type = request.GET.get("queryType")
        base_url = request.GET.get("base")
        auth = request.GET.get("auth")
        auth_pw = request.GET.get("auth_pw")
        payload = request.GET.get("payload")

        accept_header = "application/json"
        content_header = "application/json"
        headers = {
                    'Accept': accept_header,
                    'Content-Type': content_header
                   }

        try:
            if (query_type == 1):
                response = requests.get(url=base_url, auth=(auth, auth_pw), headers=headers)
            else:
                response = requests.post(url=base_url, auth=(auth, auth_pw), headers=headers, json=payload)

            context = {}
            json_response = response.json()
            print(json_response)

            template = loader.get_template('wait.html')
            return HttpResponse(template.render(context, request))

        except HTTPError as http_err:
            print(f'HTTP error occurred: {http_err}')  
        except Exception as err:
            print(f'Other error occurred: {err}')  