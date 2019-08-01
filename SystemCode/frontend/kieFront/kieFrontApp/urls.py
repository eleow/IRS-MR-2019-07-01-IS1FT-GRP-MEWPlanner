"""kieFront URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/2.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import include, path
from django.conf.urls import url
from . import views
from django.contrib.auth import views as auth_views
# from .forms import CustomAuthenticationForm
# from kieFrontApp import views

urlpatterns = [
    # Main pages
    path('home/', views.HomePageView.as_view()),
    url(r'^viewPlan/$', views.ViewPlanView.as_view(), name='viewPlan'),
    url(r'^createPlan/$', views.CreatePlanView.as_view(), name='createPlan'),

    # Debugging / Test Pages
    path('testQuery/', views.TestQueryView.as_view()),
    path('debug/', views.DebugPageView.as_view()),
    path('404/', views.ComingSoonPageView.as_view()), 
   
    # Login / Authentication Pages
    path('', auth_views.LoginView.as_view(template_name = "login.html", redirect_authenticated_user = True), name='login'),
    url(r'^signup/$', views.signup, name='signup'),
    url(r'^login/$', auth_views.LoginView.as_view(template_name = "login.html", redirect_authenticated_user = True), name='login'),
    url(r'^logout/$', auth_views.LogoutView.as_view(next_page = 'login'), name='logout'),
]
