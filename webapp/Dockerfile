FROM nginx:alpine

COPY ["css/", "/usr/share/nginx/html/css/" ]
COPY ["js/", "/usr/share/nginx/html/js/" ]
COPY ["vendor/", "/usr/share/nginx/html/vendor/" ]
COPY ["index.html", "/usr/share/nginx/html/" ]
COPY conf/nginx.conf /etc/nginx/conf.d/default.conf
