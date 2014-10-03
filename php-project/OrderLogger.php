<?php

class OrderLogger implements \Kdyby\Events\Subscriber
{

    public function getSubscribedEvents()
    {
        return array(
            "OrderService::onSave" => "logAdd", //basic nette event listener
            OrderService::class . "::onAdd" => ["logAdd", 10], //with priority
            Events::MY_FOO_EVENT, //symfony event listener
            Events::preFlush, //doctrine event listener
            "OrderService::onAddd", //undefined event
            OrderService::class . '::onStatusChanged'
        );
    }

    public function foo()
    {

    }

    public function logAdd()
    {

    }

    public function preFlush()
    {

    }


}
