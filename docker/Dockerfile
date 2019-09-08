FROM jboss/wildfly:15.0.1.Final

USER root

RUN yum update -y

RUN yum install -y epel-release
RUN yum install -y tesseract
RUN yum clean all

USER jboss

ENV TESSDATA_PREFIX /usr/share/tesseract/tessdata

ENV JBOSS_HOME /opt/jboss/wildfly

RUN $JBOSS_HOME/bin/add-user.sh admin Admin#70365 --silent

EXPOSE 9990 8080

COPY files/ocr-test-app.ear $JBOSS_HOME/standalone/deployments/

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]