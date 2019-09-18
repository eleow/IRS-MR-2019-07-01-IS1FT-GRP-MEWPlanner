# Generated by Django 2.2.3 on 2019-09-18 05:38

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('kieFrontApp', '0003_auto_20190715_1308'),
    ]

    operations = [
        migrations.AddField(
            model_name='user',
            name='cuisine_preference',
            field=models.IntegerField(blank=True, choices=[(0, 'none'), (1, 'chinese'), (2, 'malay'), (3, 'indian'), (4, 'western')], default=0, null=True),
        ),
    ]
