<?php

class OrderUpdater implements \Kdyby\Events\Subscriber
{

    public function getSubscribedEvents()
    {
        return array(
            'OrderService::onAdd',
            'OrderService::onSave' => 'sendToBackOffice',
            'OrderService::onStatusChange',
        );
    }

    public function onAdd()
    {

    }

    public function sendToBackOffice()
    {

    }

}