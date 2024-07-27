<?php
/**
 * BooksGETParameterData
 *
 * PHP version 8.1
 *
 * @package  OpenAPI\Client
 * @author   OpenAPI Generator team
 * @link     https://openapi-generator.tech
 */

/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

namespace App\QuerySerializer\Book;

/**
 * BooksGETParameterData Class Doc Comment
 *
 * @description Parameters for booksGET
 * @package  OpenAPI\Client
 * @author   OpenAPI Generator team
 * @link     https://openapi-generator.tech
 */
class BooksGETParameterData
{
    public function __construct(
        public readonly int $offset,
        public readonly int $limit
    )
    {
    }
}

