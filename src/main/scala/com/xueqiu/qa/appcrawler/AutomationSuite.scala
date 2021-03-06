package com.xueqiu.qa.appcrawler

import org.scalatest
import org.scalatest.{BeforeAndAfterAllConfigMap, ConfigMap, FunSuite, Matchers}

/**
  * Created by seveniruby on 2017/4/17.
  */
class AutomationSuite extends FunSuite with Matchers with BeforeAndAfterAllConfigMap with CommonLog {
  var crawler: Crawler = _

  override def beforeAll(configMap: ConfigMap): Unit = {
    log.info("beforeAll")
    crawler = configMap.get("crawler").get.asInstanceOf[Crawler]
  }

  test("run steps") {
    log.info("testcase start")
    val conf = crawler.conf
    val driver = crawler.driver

    val cp = new scalatest.Checkpoints.Checkpoint

    conf.testcase.steps.foreach(step => {
      val when = step.when
      val xpath = when.xpath
      val action = when.action

      driver.getListFromXPath(xpath).headOption match {
        case Some(v) => {
          val ele = URIElement(v, "Steps")
          crawler.doElementAction(ele, action)
        }
        case None => {
          log.info("not found")
          //用于生成steps的用例
          val ele = URIElement("Steps", "", "", "NOT_FOUND", xpath)
          crawler.doElementAction(ele, "")
        }
      }

      step.then.foreach(existAssert => {
        log.debug(existAssert)
        cp {
          withClue(s"${existAssert} 不存在\n") {
            driver.getListFromXPath(xpath).size should be > 0
          }
        }

      })
    })


    cp.reportAll()
    log.info("finish run steps")
  }
}
